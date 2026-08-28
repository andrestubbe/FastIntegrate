package fastintegrate.bridge;

import java.util.Map;

/**
 * Common tool interface compatible with the FastAIRuntime ecosystem.
 */
public interface FastTool {

    /**
     * Unique identifier for this tool.
     */
    String name();

    /**
     * Human/LLM-readable description of what this tool performs.
     */
    default String description() {
        return "";
    }

    /**
     * Executes the tool with the provided arguments.
     *
     * @param args invocation parameters
     * @return FastObservation containing status and output message
     */
    FastObservation execute(Map<String, Object> args);
}
