package fastintegrate.bus;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Ultra-high-throughput sidecar event bus supporting synchronous, asynchronous,
 * and ring-buffer-backed dispatch with hierarchical wildcard routing.
 */
public final class SidecarEventBus implements AutoCloseable {

    private final String busName;
    private final EventDeliveryPolicy defaultPolicy;
    private final EventBusMetrics metrics = new EventBusMetrics();
    private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();
    private final Map<String, List<Subscription>> exactTopicCache = new ConcurrentHashMap<>();
    private final List<Consumer<SidecarEvent>> interceptors = new CopyOnWriteArrayList<>();
    private final List<EventSubscriber> deadLetterSubscribers = new CopyOnWriteArrayList<>();

    private final BlockingQueue<SidecarEvent> asyncQueue;
    private final ExecutorService workerPool;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public static final class Subscription implements AutoCloseable {
        private final EventFilter filter;
        private final EventSubscriber subscriber;
        private final EventDeliveryPolicy policy;
        private final SidecarEventBus bus;

        private Subscription(EventFilter filter, EventSubscriber subscriber, EventDeliveryPolicy policy, SidecarEventBus bus) {
            this.filter = filter;
            this.subscriber = subscriber;
            this.policy = policy;
            this.bus = bus;
        }

        public EventFilter filter() {
            return filter;
        }

        public EventSubscriber subscriber() {
            return subscriber;
        }

        public EventDeliveryPolicy policy() {
            return policy;
        }

        @Override
        public void close() {
            bus.unsubscribe(this);
        }
    }

    public SidecarEventBus() {
        this("default-sidecar", EventDeliveryPolicy.SYNC, 4, 65536);
    }

    public SidecarEventBus(String busName, EventDeliveryPolicy defaultPolicy, int workerThreads, int queueCapacity) {
        this.busName = Objects.requireNonNull(busName, "busName cannot be null");
        this.defaultPolicy = Objects.requireNonNull(defaultPolicy, "defaultPolicy cannot be null");
        this.asyncQueue = new ArrayBlockingQueue<>(Math.max(128, queueCapacity));

        final ThreadFactory factory = r -> {
            Thread t = new Thread(r, "FastIntegrate-" + busName + "-worker");
            t.setDaemon(true);
            return t;
        };

        this.workerPool = Executors.newFixedThreadPool(Math.max(1, workerThreads), factory);

        for (int i = 0; i < Math.max(1, workerThreads); i++) {
            this.workerPool.submit(this::processAsyncLoop);
        }
    }

    public static SidecarEventBus create() {
        return new SidecarEventBus();
    }

    public static SidecarEventBus createAsync(int workerThreads) {
        return new SidecarEventBus("sidecar-async", EventDeliveryPolicy.ASYNC, workerThreads, 65536);
    }

    public String name() {
        return busName;
    }

    public EventBusMetrics metrics() {
        return metrics;
    }

    public Subscription subscribe(String topicPattern, EventSubscriber subscriber) {
        return subscribe(topicPattern, defaultPolicy, subscriber);
    }

    public Subscription subscribe(String topicPattern, EventDeliveryPolicy policy, EventSubscriber subscriber) {
        Objects.requireNonNull(topicPattern, "topicPattern cannot be null");
        Objects.requireNonNull(subscriber, "subscriber cannot be null");

        final EventFilter filter = EventFilter.of(topicPattern);
        final Subscription sub = new Subscription(filter, subscriber, policy != null ? policy : defaultPolicy, this);
        subscriptions.add(sub);

        if (!topicPattern.contains("*") && !topicPattern.contains("#")) {
            exactTopicCache.computeIfAbsent(topicPattern, k -> new CopyOnWriteArrayList<>()).add(sub);
        }
        return sub;
    }

    public boolean unsubscribe(Subscription subscription) {
        if (subscription == null) return false;
        boolean removed = subscriptions.remove(subscription);
        if (removed) {
            exactTopicCache.values().forEach(list -> list.remove(subscription));
        }
        return removed;
    }

    public void subscribeDeadLetter(EventSubscriber subscriber) {
        deadLetterSubscribers.add(Objects.requireNonNull(subscriber, "subscriber cannot be null"));
    }

    public void addInterceptor(Consumer<SidecarEvent> interceptor) {
        interceptors.add(Objects.requireNonNull(interceptor, "interceptor cannot be null"));
    }

    public void publish(String topic, Object payload) {
        publish(SidecarEvent.of(topic, payload));
    }

    public void publish(String topic, String source, Object payload) {
        publish(SidecarEvent.of(topic, source, payload));
    }

    public void publish(SidecarEvent event) {
        Objects.requireNonNull(event, "event cannot be null");
        metrics.recordPublish();

        for (Consumer<SidecarEvent> interceptor : interceptors) {
            try {
                interceptor.accept(event);
            } catch (Throwable ignored) {
            }
        }

        List<Subscription> matching = findMatchingSubscriptions(event.topic());
        if (matching.isEmpty()) {
            metrics.recordDropped();
            dispatchDeadLetter(event);
            return;
        }

        for (Subscription sub : matching) {
            dispatchToSubscription(sub, event);
        }
    }

    public void publishBatch(Collection<SidecarEvent> events) {
        if (events == null || events.isEmpty()) return;
        for (SidecarEvent event : events) {
            publish(event);
        }
    }

    private List<Subscription> findMatchingSubscriptions(String topic) {
        List<Subscription> exact = exactTopicCache.get(topic);
        List<Subscription> matches = null;

        if (exact != null && !exact.isEmpty()) {
            matches = new ArrayList<>(exact);
        }

        for (Subscription sub : subscriptions) {
            if (!sub.filter.matches(topic)) {
                continue;
            }
            if (matches == null) {
                matches = new ArrayList<>();
            }
            if (!matches.contains(sub)) {
                matches.add(sub);
            }
        }

        return matches != null ? matches : Collections.emptyList();
    }

    private void dispatchToSubscription(Subscription sub, SidecarEvent event) {
        EventDeliveryPolicy policy = sub.policy;
        if (policy == EventDeliveryPolicy.SYNC) {
            executeDirect(sub.subscriber, event);
        } else if (policy == EventDeliveryPolicy.ASYNC) {
            if (!running.get()) return;
            try {
                asyncQueue.put(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                metrics.recordFailure();
            }
        } else if (policy == EventDeliveryPolicy.NON_BLOCKING_ASYNC) {
            if (!running.get()) return;
            boolean offered = asyncQueue.offer(event);
            if (!offered) {
                metrics.recordDropped();
                dispatchDeadLetter(event);
            }
        }
    }

    private void executeDirect(EventSubscriber subscriber, SidecarEvent event) {
        long start = System.nanoTime();
        try {
            subscriber.onEvent(event);
            metrics.recordDelivery(System.nanoTime() - start);
        } catch (Throwable t) {
            metrics.recordFailure();
            dispatchDeadLetter(event);
        }
    }

    private void dispatchDeadLetter(SidecarEvent event) {
        for (EventSubscriber dlq : deadLetterSubscribers) {
            try {
                dlq.onEvent(event);
            } catch (Throwable ignored) {
            }
        }
    }

    private void processAsyncLoop() {
        while (running.get() || !asyncQueue.isEmpty()) {
            try {
                SidecarEvent event = asyncQueue.poll(50, TimeUnit.MILLISECONDS);
                if (event != null) {
                    List<Subscription> matching = findMatchingSubscriptions(event.topic());
                    for (Subscription sub : matching) {
                        executeDirect(sub.subscriber, event);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable ignored) {
            }
        }
    }

    public int activeSubscriptionCount() {
        return subscriptions.size();
    }

    public int queueDepth() {
        return asyncQueue.size();
    }

    @Override
    public void close() {
        if (running.compareAndSet(true, false)) {
            workerPool.shutdown();
            try {
                if (!workerPool.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                    workerPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                workerPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            subscriptions.clear();
            exactTopicCache.clear();
            interceptors.clear();
            deadLetterSubscribers.clear();
        }
    }
}
