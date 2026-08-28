package fastintegrate.bus;

/**
 * Dispatch mode for the sidecar event bus.
 */
public enum EventDeliveryPolicy {
    /**
     * Executes subscriber handlers directly on the publisher thread.
     */
    SYNC,

    /**
     * Enqueues events onto the sidecar worker queue for asynchronous background execution.
     */
    ASYNC,

    /**
     * Non-blocking broadcast with overflow protection (drops or routes to DLQ if queue is saturated).
     */
    NON_BLOCKING_ASYNC
}
