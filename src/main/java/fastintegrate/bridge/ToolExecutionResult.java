package fastintegrate.bridge;

/**
 * Standard observation and execution result record.
 */
public record ToolExecutionResult(
        String toolName,
        boolean success,
        String output,
        long durationNs
) implements FastObservation {

    @Override
    public String message() {
        return output;
    }

    public static ToolExecutionResult ok(String toolName, String output, long durationNs) {
        return new ToolExecutionResult(toolName, true, output, durationNs);
    }

    public static ToolExecutionResult error(String toolName, String error, long durationNs) {
        return new ToolExecutionResult(toolName, false, error, durationNs);
    }

    public double durationMicros() {
        return durationNs / 1_000.0;
    }

    public double durationMillis() {
        return durationNs / 1_000_000.0;
    }
}
