package fastintegrate.webhook;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Route definition for handling webhooks with dynamic path parameters and signature verification.
 */
public final class WebhookRoute {

    private final String method;
    private final String pathPattern;
    private final WebhookHandler handler;
    private final HmacValidator hmacValidator;
    private final String signatureHeader;
    private final String autoForwardTopic;

    private final Pattern compiledPattern;
    private final List<String> parameterNames;

    public WebhookRoute(String method, String pathPattern, WebhookHandler handler,
                        HmacValidator hmacValidator, String signatureHeader, String autoForwardTopic) {
        this.method = method != null ? method.toUpperCase() : "POST";
        this.pathPattern = Objects.requireNonNull(pathPattern, "pathPattern cannot be null");
        this.handler = handler;
        this.hmacValidator = hmacValidator;
        this.signatureHeader = signatureHeader;
        this.autoForwardTopic = autoForwardTopic;

        this.parameterNames = new ArrayList<>();
        this.compiledPattern = compilePathPattern(pathPattern, this.parameterNames);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String method() {
        return method;
    }

    public String pathPattern() {
        return pathPattern;
    }

    public WebhookHandler handler() {
        return handler;
    }

    public HmacValidator hmacValidator() {
        return hmacValidator;
    }

    public String signatureHeader() {
        return signatureHeader;
    }

    public String autoForwardTopic() {
        return autoForwardTopic;
    }

    public Map<String, String> match(String requestMethod, String requestPath) {
        if (!this.method.equalsIgnoreCase(requestMethod)) {
            return null;
        }
        Matcher matcher = compiledPattern.matcher(requestPath);
        if (!matcher.matches()) {
            return null;
        }

        if (parameterNames.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> params = new HashMap<>(parameterNames.size());
        for (int i = 0; i < parameterNames.size(); i++) {
            params.put(parameterNames.get(i), matcher.group(i + 1));
        }
        return params;
    }

    private static Pattern compilePathPattern(String pattern, List<String> outParams) {
        StringBuilder regex = new StringBuilder("^");
        String[] tokens = pattern.split("/");
        for (String token : tokens) {
            if (token.isEmpty()) continue;
            regex.append("/");
            if (token.startsWith("{") && token.endsWith("}")) {
                String paramName = token.substring(1, token.length() - 1);
                outParams.add(paramName);
                regex.append("([^/]+)");
            } else if (token.equals("*")) {
                regex.append("[^/]+");
            } else if (token.equals("**")) {
                regex.append(".*");
            } else {
                regex.append(Pattern.quote(token));
            }
        }
        if (pattern.endsWith("/") && !regex.toString().endsWith("/")) {
            regex.append("/");
        }
        regex.append("$");
        return Pattern.compile(regex.toString());
    }

    public static final class Builder {
        private String method = "POST";
        private String pathPattern;
        private WebhookHandler handler;
        private HmacValidator hmacValidator;
        private String signatureHeader;
        private String autoForwardTopic;

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder path(String pathPattern) {
            this.pathPattern = pathPattern;
            return this;
        }

        public Builder handler(WebhookHandler handler) {
            this.handler = handler;
            return this;
        }

        public Builder verifyHmac(String signatureHeader, HmacValidator validator) {
            this.signatureHeader = signatureHeader;
            this.hmacValidator = validator;
            return this;
        }

        public Builder autoForwardTo(String topic) {
            this.autoForwardTopic = topic;
            return this;
        }

        public WebhookRoute build() {
            return new WebhookRoute(method, pathPattern, handler, hmacValidator, signatureHeader, autoForwardTopic);
        }
    }
}
