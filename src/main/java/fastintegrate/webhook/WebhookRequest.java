package fastintegrate.webhook;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable representation of an incoming Webhook HTTP/RPC request.
 */
public final class WebhookRequest {

    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final Map<String, String> pathParams;
    private final byte[] bodyBytes;
    private final long receivedTimestampNs;

    public WebhookRequest(String method, String path, Map<String, String> headers,
                          Map<String, String> queryParams, Map<String, String> pathParams,
                          byte[] bodyBytes, long receivedTimestampNs) {
        this.method = method != null ? method.toUpperCase() : "POST";
        this.path = Objects.requireNonNull(path, "path cannot be null");
        this.headers = headers != null ? Collections.unmodifiableMap(new HashMap<>(headers)) : Collections.emptyMap();
        this.queryParams = queryParams != null ? Collections.unmodifiableMap(new HashMap<>(queryParams)) : Collections.emptyMap();
        this.pathParams = pathParams != null ? Collections.unmodifiableMap(new HashMap<>(pathParams)) : Collections.emptyMap();
        this.bodyBytes = bodyBytes != null ? bodyBytes : new byte[0];
        this.receivedTimestampNs = receivedTimestampNs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static WebhookRequest of(String path, String jsonBody) {
        return builder().path(path).body(jsonBody).header("Content-Type", "application/json").build();
    }

    public String method() {
        return method;
    }

    public String path() {
        return path;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public String header(String name) {
        if (name == null) return null;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Map<String, String> queryParams() {
        return queryParams;
    }

    public String queryParam(String name) {
        return queryParams.get(name);
    }

    public Map<String, String> pathParams() {
        return pathParams;
    }

    public String pathParam(String name) {
        return pathParams.get(name);
    }

    public byte[] bodyBytes() {
        return bodyBytes;
    }

    public String bodyAsString() {
        return new String(bodyBytes, StandardCharsets.UTF_8);
    }

    public long receivedTimestampNs() {
        return receivedTimestampNs;
    }

    public WebhookRequest withPathParams(Map<String, String> pathParams) {
        return new WebhookRequest(this.method, this.path, this.headers, this.queryParams, pathParams, this.bodyBytes, this.receivedTimestampNs);
    }

    @Override
    public String toString() {
        return "WebhookRequest{method='" + method + "', path='" + path + "', bodyLength=" + bodyBytes.length + "}";
    }

    public static final class Builder {
        private String method = "POST";
        private String path = "/";
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> queryParams = new HashMap<>();
        private Map<String, String> pathParams = new HashMap<>();
        private byte[] bodyBytes = new byte[0];
        private long receivedTimestampNs = System.nanoTime();

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            if (headers != null) this.headers.putAll(headers);
            return this;
        }

        public Builder queryParam(String name, String value) {
            this.queryParams.put(name, value);
            return this;
        }

        public Builder pathParam(String name, String value) {
            this.pathParams.put(name, value);
            return this;
        }

        public Builder body(byte[] body) {
            this.bodyBytes = body != null ? body : new byte[0];
            return this;
        }

        public Builder body(String body) {
            this.bodyBytes = body != null ? body.getBytes(StandardCharsets.UTF_8) : new byte[0];
            return this;
        }

        public Builder timestampNs(long timestampNs) {
            this.receivedTimestampNs = timestampNs;
            return this;
        }

        public WebhookRequest build() {
            return new WebhookRequest(method, path, headers, queryParams, pathParams, bodyBytes, receivedTimestampNs);
        }
    }
}
