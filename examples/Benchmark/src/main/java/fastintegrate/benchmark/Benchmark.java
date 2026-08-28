package fastintegrate.benchmark;

import fastintegrate.bridge.*;
import fastintegrate.bus.SidecarEvent;
import fastintegrate.bus.SidecarEventBus;
import fastintegrate.webhook.*;
import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class Benchmark {

    private SidecarEventBus eventBus;
    private SidecarEventBus wildcardBus;
    private SidecarEvent prebuiltEvent;

    private HmacValidator hmacValidator;
    private byte[] rawPayload;
    private String signatureHex;

    private WebhookRouter webhookRouter;
    private WebhookRequest webhookRequest;

    private ToolBindingBridge toolBridge;
    private Map<String, Object> toolArgs;

    @Setup(Level.Trial)
    public void setup() {
        // 1. EventBus Setup
        eventBus = SidecarEventBus.create();
        eventBus.subscribe("agent.status.update", event -> {});
        prebuiltEvent = SidecarEvent.of("agent.status.update", "ping");

        wildcardBus = SidecarEventBus.create();
        wildcardBus.subscribe("agent.#", event -> {});

        // 2. HMAC & Webhook Setup
        String secret = "production-benchmark-secret-key-32b";
        hmacValidator = HmacValidator.sha256(secret);
        rawPayload = "{\"event\":\"order_created\",\"amount\":2500}".getBytes();
        signatureHex = "sha256=" + hmacValidator.computeHex(rawPayload);

        webhookRouter = WebhookRouter.create();
        webhookRouter.postSecure("/api/{service}/{action}", "X-Hub-Signature-256", secret,
                req -> WebhookResponse.ok("handled"));
        webhookRequest = WebhookRequest.builder()
                .path("/api/payment/process")
                .header("X-Hub-Signature-256", signatureHex)
                .body(rawPayload)
                .build();

        // 3. Tool Bridge Setup
        toolBridge = ToolBindingBridge.create();
        toolBridge.registerTool(new FastTool() {
            @Override
            public String name() { return "fast_math"; }
            @Override
            public FastObservation execute(Map<String, Object> args) {
                int a = (int) args.getOrDefault("a", 1);
                return ToolExecutionResult.ok(name(), String.valueOf(a * 2), 10);
            }
        });
        toolArgs = Map.of("a", 21);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        eventBus.close();
        wildcardBus.close();
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void benchmarkEventBusDirectPublish() {
        eventBus.publish(prebuiltEvent);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void benchmarkEventBusWildcardPublish() {
        wildcardBus.publish(prebuiltEvent);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public boolean benchmarkHmacSha256Verification() {
        return hmacValidator.validateHex(rawPayload, signatureHex);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public WebhookResponse benchmarkWebhookRouterDispatch() {
        return webhookRouter.dispatch(webhookRequest);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public FastObservation benchmarkToolBridgeExecution() {
        return toolBridge.execute("fast_math", toolArgs);
    }
}
