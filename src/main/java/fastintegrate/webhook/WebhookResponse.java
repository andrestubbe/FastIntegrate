package fastintegrate.webhook;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard response object returned by Webhook handlers and the WebhookRouter.
 */
public final class WebhookResponse {

    private final int statusCode;
    private final Map<String, String> headers;
    private final String body;

    public WebhookResponse(int statusCode, Map<String, String> headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers != null ? Collections.unmodifiableMap(new HashMap<>(headers)) : Collections.emptyMap();
        this.body = body != null ? body : "";
    }

    public static WebhookResponse ok() {
        return new WebhookResponse(200, Map.of("Content-Type", "application/json"), "{\"status\":\"ok\"}");
    }

    public static WebhookResponse ok(String body) {
        return new WebhookResponse(200, Map.of("Content-Type", "application/json"), body);
    }

    public static WebhookResponse accepted() {
        return new WebhookResponse(202, Map.of("Content-Type", "application/json"), "{\"status\":\"accepted\"}");
    }

    public static WebhookResponse badRequest(String message) {
        return new WebhookResponse(400, Map.of("Content-Type", "application/json"), "{\"error\":\"bad_request\",\"message\":\"" + escapeJson(message) + "\"}");
    }

    public static WebhookResponse unauthorized(String reason) {
        return new WebhookResponse(401, Map.of("Content-Type", "application/json"), "{\"error\":\"unauthorized\",\"reason\":\"" + escapeJson(reason) + "\"}");
    }

    public static WebhookResponse forbidden(String reason) {
        return new WebhookResponse(403, Map.of("Content-Type", "application/json"), "{\"error\":\"forbidden\",\"reason\":\"" + escapeJson(reason) + "\"}");
    }

    public static WebhookResponse notFound() {
        return new WebhookResponse(404, Map.of("Content-Type", "application/json"), "{\"error\":\"not_found\"}");
    }

    public static WebhookResponse serverError(String message) {
        return new WebhookResponse(500, Map.of("Content-Type", "application/json"), "{\"error\":\"internal_server_error\",\"message\":\"" + escapeJson(message) + "\"}");
    }

    public int statusCode() {
        return statusCode;
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public String body() {
        return body;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    @Override
    public String toString() {
        return "WebhookResponse{status=" + statusCode + ", body='" + body + "'}";
    }
}
