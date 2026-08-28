package fastintegrate.bus;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Real-time operational metrics for SidecarEventBus.
 */
public final class EventBusMetrics {

    private final AtomicLong publishedCount = new AtomicLong(0);
    private final AtomicLong deliveredCount = new AtomicLong(0);
    private final AtomicLong failedCount = new AtomicLong(0);
    private final AtomicLong droppedCount = new AtomicLong(0);
    private final AtomicLong totalDeliveryLatencyNs = new AtomicLong(0);

    public void recordPublish() {
        publishedCount.incrementAndGet();
    }

    public void recordDelivery(long latencyNs) {
        deliveredCount.incrementAndGet();
        totalDeliveryLatencyNs.addAndGet(latencyNs);
    }

    public void recordFailure() {
        failedCount.incrementAndGet();
    }

    public void recordDropped() {
        droppedCount.incrementAndGet();
    }

    public long publishedCount() {
        return publishedCount.get();
    }

    public long deliveredCount() {
        return deliveredCount.get();
    }

    public long failedCount() {
        return failedCount.get();
    }

    public long droppedCount() {
        return droppedCount.get();
    }

    public double averageLatencyNanos() {
        long delivered = deliveredCount.get();
        return delivered == 0 ? 0.0 : (double) totalDeliveryLatencyNs.get() / delivered;
    }

    public double averageLatencyMicros() {
        return averageLatencyNanos() / 1_000.0;
    }

    public void reset() {
        publishedCount.set(0);
        deliveredCount.set(0);
        failedCount.set(0);
        droppedCount.set(0);
        totalDeliveryLatencyNs.set(0);
    }

    @Override
    public String toString() {
        return String.format("Metrics[published=%d, delivered=%d, failed=%d, dropped=%d, avgLatency=%.2f µs]",
                publishedCount.get(), deliveredCount.get(), failedCount.get(), droppedCount.get(), averageLatencyMicros());
    }
}
