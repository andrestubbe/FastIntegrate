package fastintegrate.webhook;

/**
 * Functional interface for handling an incoming Webhook request.
 */
@FunctionalInterface
public interface WebhookHandler {

    /**
     * Dispatches the webhook request and produces a response.
     *
     * @param request the received WebhookRequest
     * @return the resulting WebhookResponse
     * @throws Exception if processing fails
     */
    WebhookResponse handle(WebhookRequest request) throws Exception;
}
