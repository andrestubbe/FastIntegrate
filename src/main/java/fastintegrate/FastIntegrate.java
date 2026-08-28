package fastintegrate;

import fastintegrate.bridge.ToolBindingBridge;
import fastintegrate.bus.EventDeliveryPolicy;
import fastintegrate.bus.SidecarEventBus;
import fastintegrate.webhook.WebhookRouter;

import java.util.Objects;

/**
 * FastIntegrate — Central Unified Integration Facade.
 *
 * Coordinates the SidecarEventBus, High-Throughput Webhook Router,
 * and FastAIRuntime / FastAIMCP Tool Binding Bridge.
 */
public final class FastIntegrate implements AutoCloseable {

    private final SidecarEventBus eventBus;
    private final WebhookRouter webhookRouter;
    private final ToolBindingBridge toolBridge;

    private FastIntegrate(SidecarEventBus eventBus, WebhookRouter webhookRouter, ToolBindingBridge toolBridge) {
        this.eventBus = eventBus != null ? eventBus : SidecarEventBus.create();
        this.webhookRouter = webhookRouter != null ? webhookRouter : WebhookRouter.create(this.eventBus);
        this.toolBridge = toolBridge != null ? toolBridge : ToolBindingBridge.create(this.eventBus);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static FastIntegrate create() {
        return builder().build();
    }

    public SidecarEventBus eventBus() {
        return eventBus;
    }

    public WebhookRouter webhookRouter() {
        return webhookRouter;
    }

    public ToolBindingBridge toolBridge() {
        return toolBridge;
    }

    @Override
    public void close() {
        eventBus.close();
    }

    public static final class Builder {
        private String busName = "fastintegrate-sidecar";
        private EventDeliveryPolicy defaultDeliveryPolicy = EventDeliveryPolicy.SYNC;
        private int workerThreads = 4;
        private int queueCapacity = 65536;
        private SidecarEventBus customEventBus;
        private WebhookRouter customWebhookRouter;
        private ToolBindingBridge customToolBridge;

        public Builder busName(String name) {
            this.busName = name;
            return this;
        }

        public Builder deliveryPolicy(EventDeliveryPolicy policy) {
            this.defaultDeliveryPolicy = policy;
            return this;
        }

        public Builder workerThreads(int threads) {
            this.workerThreads = threads;
            return this;
        }

        public Builder queueCapacity(int capacity) {
            this.queueCapacity = capacity;
            return this;
        }

        public Builder eventBus(SidecarEventBus bus) {
            this.customEventBus = bus;
            return this;
        }

        public Builder webhookRouter(WebhookRouter router) {
            this.customWebhookRouter = router;
            return this;
        }

        public Builder toolBridge(ToolBindingBridge bridge) {
            this.customToolBridge = bridge;
            return this;
        }

        public FastIntegrate build() {
            SidecarEventBus bus = customEventBus != null
                    ? customEventBus
                    : new SidecarEventBus(busName, defaultDeliveryPolicy, workerThreads, queueCapacity);

            WebhookRouter router = customWebhookRouter != null
                    ? customWebhookRouter
                    : WebhookRouter.create(bus);

            ToolBindingBridge bridge = customToolBridge != null
                    ? customToolBridge
                    : ToolBindingBridge.create(bus);

            return new FastIntegrate(bus, router, bridge);
        }
    }
}
