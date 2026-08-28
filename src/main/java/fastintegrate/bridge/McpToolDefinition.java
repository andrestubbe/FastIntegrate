package fastintegrate.bridge;

import java.util.Collections;
import java.util.Map;

/**
 * Standard MCP Tool schema representation compatible with FastAIMCP protocol.
 */
public record McpToolDefinition(
        String name,
        String description,
        Map<String, Object> inputSchema
) {
    public McpToolDefinition {
        inputSchema = inputSchema != null ? Collections.unmodifiableMap(inputSchema) : Collections.emptyMap();
    }

    public static McpToolDefinition of(String name, String description) {
        return new McpToolDefinition(name, description, Map.of(
                "type", "object",
                "properties", Collections.emptyMap()
        ));
    }

    public static McpToolDefinition of(String name, String description, Map<String, Object> schema) {
        return new McpToolDefinition(name, description, schema);
    }
}
