package fastintegrate;

import fastintegrate.bus.SidecarEvent;
import fastintegrate.bus.SidecarEventBus;
import fastintegrate.webhook.HmacValidator;
import fastintegrate.webhook.WebhookRequest;
import fastintegrate.webhook.WebhookResponse;
import fastintegrate.webhook.WebhookRouter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WebhookRouterTest {

    @Test
    public void testBasicRoutingAndPathParams() {
        WebhookRouter router = WebhookRouter.create();
        router.post("/api/{service}/{action}", request -> {
            String service = request.pathParam("service");
            String action = request.pathParam("action");
            return WebhookResponse.ok("{\"processed\":\"" + service + ":" + action + "\"}");
        });

        WebhookRequest request = WebhookRequest.of("/api/auth/login", "{\"user\":\"admin\"}");
        WebhookResponse response = router.dispatch(request);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("auth:login"));
    }

    @Test
    public void testHmacValidationSuccessAndFailure() {
        String secret = "super-secret-key-123";
        HmacValidator validator = HmacValidator.sha256(secret);

        WebhookRouter router = WebhookRouter.create();
        router.postSecure("/webhook/github", "X-Hub-Signature-256", secret, request -> WebhookResponse.ok("verified"));

        String payload = "{\"ref\":\"refs/heads/main\"}";
        String validSig = "sha256=" + validator.computeHex(payload);

        // 1. Valid Signature
        WebhookRequest validReq = WebhookRequest.builder()
                .path("/webhook/github")
                .header("X-Hub-Signature-256", validSig)
                .body(payload)
                .build();
        WebhookResponse validRes = router.dispatch(validReq);
        assertEquals(200, validRes.statusCode());
        assertEquals("verified", validRes.body());

        // 2. Invalid Signature
        WebhookRequest invalidReq = WebhookRequest.builder()
                .path("/webhook/github")
                .header("X-Hub-Signature-256", "sha256=invalidhash000000000000000000000000000000000000000000000000000000")
                .body(payload)
                .build();
        WebhookResponse invalidRes = router.dispatch(invalidReq);
        assertEquals(401, invalidRes.statusCode());
    }

    @Test
    public void testAutoForwardToEventBus() {
        try (SidecarEventBus bus = SidecarEventBus.create()) {
            List<SidecarEvent> received = new ArrayList<>();
            bus.subscribe("events.stripe.charge_captured", received::add);

            WebhookRouter router = WebhookRouter.create(bus);
            router.forward("/webhooks/{provider}/{event}", "events.{provider}.{event}");

            WebhookRequest req = WebhookRequest.of("/webhooks/stripe/charge_captured", "{\"amount\":4200}");
            WebhookResponse res = router.dispatch(req);

            assertEquals(202, res.statusCode());
            assertEquals(1, received.size());
            assertEquals("events.stripe.charge_captured", received.get(0).topic());
            assertEquals("{\"amount\":4200}", received.get(0).payload());
        }
    }

    @Test
    public void testNotFoundRoute() {
        WebhookRouter router = WebhookRouter.create();
        WebhookRequest req = WebhookRequest.of("/unmapped/route", "{}");
        WebhookResponse res = router.dispatch(req);
        assertEquals(404, res.statusCode());
    }
}
