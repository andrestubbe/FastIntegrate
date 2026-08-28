package fastintegrate.webhook;

import fastintegrate.bus.SidecarEvent;
import fastintegrate.bus.SidecarEventBus;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * High-performance Webhook Dispatcher and Router with HMAC verification,
 * path templating, and automated SidecarEventBus forwarding.
 */
public final class WebhookRouter {

    private final List<WebhookRoute> routes = new CopyOnWriteArrayList<>();
    private final List<Consumer<WebhookRequest>> beforeFilters = new CopyOnWriteArrayList<>();
    private final SidecarEventBus eventBus;

    public WebhookRouter() {
        this(null);
    }

    public WebhookRouter(SidecarEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public static WebhookRouter create() {
        return new WebhookRouter(null);
    }

    public static WebhookRouter create(SidecarEventBus eventBus) {
        return new WebhookRouter(eventBus);
    }

    public WebhookRouter register(WebhookRoute route) {
        routes.add(Objects.requireNonNull(route, "route cannot be null"));
        return this;
    }

    public WebhookRouter post(String path, WebhookHandler handler) {
        return register(WebhookRoute.builder().method("POST").path(path).handler(handler).build());
    }

    public WebhookRouter postSecure(String path, String signatureHeader, String hmacSecret, WebhookHandler handler) {
        return register(WebhookRoute.builder()
                .method("POST")
                .path(path)
                .verifyHmac(signatureHeader, HmacValidator.sha256(hmacSecret))
                .handler(handler)
                .build());
    }

    public WebhookRouter forward(String path, String eventTopic) {
        return register(WebhookRoute.builder()
                .method("POST")
                .path(path)
                .autoForwardTo(eventTopic)
                .build());
    }

    public WebhookRouter forwardSecure(String path, String signatureHeader, String hmacSecret, String eventTopic) {
        return register(WebhookRoute.builder()
                .method("POST")
                .path(path)
                .verifyHmac(signatureHeader, HmacValidator.sha256(hmacSecret))
                .autoForwardTo(eventTopic)
                .build());
    }

    public WebhookRouter before(Consumer<WebhookRequest> filter) {
        beforeFilters.add(Objects.requireNonNull(filter, "filter cannot be null"));
        return this;
    }

    /**
     * Dispatches an incoming webhook request through route matching and verification.
     */
    public WebhookResponse dispatch(WebhookRequest request) {
        Objects.requireNonNull(request, "request cannot be null");

        for (Consumer<WebhookRequest> filter : beforeFilters) {
            try {
                filter.accept(request);
            } catch (Throwable t) {
                return WebhookResponse.badRequest("Filter execution rejected request: " + t.getMessage());
            }
        }

        for (WebhookRoute route : routes) {
            Map<String, String> matchedParams = route.match(request.method(), request.path());
            if (matchedParams == null) {
                continue;
            }

            WebhookRequest populatedRequest = matchedParams.isEmpty() ? request : request.withPathParams(matchedParams);

            // HMAC validation
            if (route.hmacValidator() != null && route.signatureHeader() != null) {
                String signature = populatedRequest.header(route.signatureHeader());
                if (signature == null || !route.hmacValidator().validateHex(populatedRequest.bodyBytes(), signature)) {
                    return WebhookResponse.unauthorized("Invalid HMAC signature on header '" + route.signatureHeader() + "'");
                }
            }

            // Auto forwarding to SidecarEventBus
            if (route.autoForwardTopic() != null && eventBus != null) {
                String resolvedTopic = resolveTopic(route.autoForwardTopic(), populatedRequest.pathParams());
                SidecarEvent event = SidecarEvent.builder()
                        .topic(resolvedTopic)
                        .source("webhook:" + request.path())
                        .payload(populatedRequest.bodyAsString())
                        .headers(populatedRequest.headers())
                        .build();
                eventBus.publish(event);
            }

            if (route.handler() != null) {
                try {
                    return route.handler().handle(populatedRequest);
                } catch (Throwable t) {
                    return WebhookResponse.serverError("Handler execution failed: " + t.getMessage());
                }
            }

            return WebhookResponse.accepted();
        }

        return WebhookResponse.notFound();
    }

    private String resolveTopic(String topicPattern, Map<String, String> pathParams) {
        if (pathParams.isEmpty() || !topicPattern.contains("{")) {
            return topicPattern;
        }
        String resolved = topicPattern;
        for (Map.Entry<String, String> entry : pathParams.entrySet()) {
            resolved = resolved.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return resolved;
    }

    public List<WebhookRoute> routes() {
        return Collections.unmodifiableList(routes);
    }
}
