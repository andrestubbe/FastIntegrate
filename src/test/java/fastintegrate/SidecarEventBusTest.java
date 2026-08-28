package fastintegrate;

import fastintegrate.bus.EventDeliveryPolicy;
import fastintegrate.bus.EventFilter;
import fastintegrate.bus.SidecarEvent;
import fastintegrate.bus.SidecarEventBus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class SidecarEventBusTest {

    @Test
    public void testSyncPublishAndSubscribe() {
        SidecarEventBus bus = SidecarEventBus.create();
        List<SidecarEvent> received = new ArrayList<>();

        bus.subscribe("orders.created", received::add);
        bus.publish("orders.created", "Order#1001");

        assertEquals(1, received.size());
        assertEquals("orders.created", received.get(0).topic());
        assertEquals("Order#1001", received.get(0).payload());
        assertEquals(1, bus.metrics().publishedCount());
        assertEquals(1, bus.metrics().deliveredCount());
        bus.close();
    }

    @Test
    public void testWildcardTopicMatching() {
        EventFilter exact = EventFilter.of("ai.agent.query");
        assertTrue(exact.matches("ai.agent.query"));
        assertFalse(exact.matches("ai.agent.response"));

        EventFilter singleWildcard = EventFilter.of("ai.*.query");
        assertTrue(singleWildcard.matches("ai.agent.query"));
        assertTrue(singleWildcard.matches("ai.bot.query"));
        assertFalse(singleWildcard.matches("ai.agent.sub.query"));

        EventFilter multiWildcard = EventFilter.of("ai.#");
        assertTrue(multiWildcard.matches("ai.agent"));
        assertTrue(multiWildcard.matches("ai.agent.sub.query"));
        assertFalse(multiWildcard.matches("web.request"));
    }

    @Test
    public void testAsyncDispatch() throws InterruptedException {
        try (SidecarEventBus bus = SidecarEventBus.createAsync(2)) {
            CountDownLatch latch = new CountDownLatch(5);
            AtomicInteger count = new AtomicInteger(0);

            bus.subscribe("metrics.#", EventDeliveryPolicy.ASYNC, event -> {
                count.incrementAndGet();
                latch.countDown();
            });

            for (int i = 0; i < 5; i++) {
                bus.publish("metrics.cpu.load", "load=" + i);
            }

            assertTrue(latch.await(3, TimeUnit.SECONDS));
            assertEquals(5, count.get());
        }
    }

    @Test
    public void testDeadLetterQueue() {
        try (SidecarEventBus bus = SidecarEventBus.create()) {
            List<SidecarEvent> dlq = new ArrayList<>();
            bus.subscribeDeadLetter(dlq::add);

            bus.publish("unregistered.topic", "unhandled payload");

            assertEquals(1, dlq.size());
            assertEquals("unregistered.topic", dlq.get(0).topic());
            assertEquals(1, bus.metrics().droppedCount());
        }
    }

    @Test
    public void testUnsubscribe() {
        SidecarEventBus bus = SidecarEventBus.create();
        AtomicInteger counter = new AtomicInteger();

        var sub = bus.subscribe("ping", e -> counter.incrementAndGet());
        bus.publish("ping", "1");
        assertEquals(1, counter.get());

        sub.close();
        bus.publish("ping", "2");
        assertEquals(1, counter.get());
        bus.close();
    }
}
