package fastintegrate.bridge;

/**
 * Common observation interface compatible with FastAIRuntime.
 */
public interface FastObservation {

    /**
     * Whether the tool execution succeeded.
     */
    boolean success();

    /**
     * The textual output or error message.
     */
    String message();
}
