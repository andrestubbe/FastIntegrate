package fastintegrate.bus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Immutable representation of an event transmitted through the SidecarEventBus.
 */
public final class SidecarEvent {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    private final long id;
    private final String topic;
    private final String source;
    private final Object payload;
    private final long timestampNs;
    private final Map<String, String> headers;
    private final String correlationId;

    public SidecarEvent(long id, String topic, String source, Object payload, long timestampNs,
                        Map<String, String> headers, String correlationId) {
        this.id = id;
        this.topic = Objects.requireNonNull(topic, "topic cannot be null");
        this.source = source != null ? source : "anonymous";
        this.payload = payload;
        this.timestampNs = timestampNs;
        this.headers = headers != null ? Collections.unmodifiableMap(new HashMap<>(headers)) : Collections.emptyMap();
        this.correlationId = correlationId != null ? correlationId : String.valueOf(id);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static SidecarEvent of(String topic, Object payload) {
        return builder().topic(topic).payload(payload).build();
    }

    public static SidecarEvent of(String topic, String source, Object payload) {
        return builder().topic(topic).source(source).payload(payload).build();
    }

    public long id() {
        return id;
    }

    public String topic() {
        return topic;
    }

    public String source() {
        return source;
    }

    public Object payload() {
        return payload;
    }

    public <T> T payload(Class<T> type) {
        if (payload == null) {
            return null;
        }
        if (type.isInstance(payload)) {
            return type.cast(payload);
        }
        throw new ClassCastException("Payload of type " + payload.getClass().getName() + " cannot be cast to " + type.getName());
    }

    public long timestampNs() {
        return timestampNs;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public String header(String key) {
        return headers.get(key);
    }

    public String correlationId() {
        return correlationId;
    }

    @Override
    public String toString() {
        return "SidecarEvent{id=" + id + ", topic='" + topic + "', source='" + source + "', correlationId='" + correlationId + "'}";
    }

    public static final class Builder {
        private long id = ID_GENERATOR.getAndIncrement();
        private String topic;
        private String source = "system";
        private Object payload;
        private long timestampNs = System.nanoTime();
        private Map<String, String> headers = new HashMap<>();
        private String correlationId;

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public Builder timestampNs(long timestampNs) {
            this.timestampNs = timestampNs;
            return this;
        }

        public Builder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            if (headers != null) {
                this.headers.putAll(headers);
            }
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public SidecarEvent build() {
            if (topic == null || topic.isBlank()) {
                throw new IllegalArgumentException("topic must not be null or blank");
            }
            return new SidecarEvent(id, topic, source, payload, timestampNs, headers, correlationId);
        }
    }
}
