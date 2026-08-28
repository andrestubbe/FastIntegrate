package fastintegrate.bridge;

import fastintegrate.bus.SidecarEvent;
import fastintegrate.bus.SidecarEventBus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Universal Tool Binding Bridge connecting FastAIRuntime deterministic tools,
 * FastAIMCP protocol endpoints, and SidecarEventBus reactive invocations.
 */
public final class ToolBindingBridge {

    private final Map<String, FastTool> tools = new ConcurrentHashMap<>();
    private final Map<String, McpToolDefinition> mcpSchemas = new ConcurrentHashMap<>();
    private final SidecarEventBus eventBus;

    public ToolBindingBridge() {
        this(null);
    }

    public ToolBindingBridge(SidecarEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public static ToolBindingBridge create() {
        return new ToolBindingBridge(null);
    }

    public static ToolBindingBridge create(SidecarEventBus eventBus) {
        return new ToolBindingBridge(eventBus);
    }

    /**
     * Registers a native FastAIRuntime tool with an optional MCP schema.
     */
    public ToolBindingBridge registerTool(FastTool tool) {
        return registerTool(tool, McpToolDefinition.of(tool.name(), tool.description()));
    }

    /**
     * Registers a native FastAIRuntime tool with an explicit MCP schema.
     */
    public ToolBindingBridge registerTool(FastTool tool, McpToolDefinition schema) {
        Objects.requireNonNull(tool, "tool cannot be null");
        tools.put(tool.name(), tool);
        if (schema != null) {
            mcpSchemas.put(tool.name(), schema);
        }
        return this;
    }

    /**
     * Registers a remote or external MCP tool and binds it as a local executable FastTool.
     */
    public ToolBindingBridge registerMcpTool(McpToolDefinition schema, Function<Map<String, Object>, String> executor) {
        Objects.requireNonNull(schema, "schema cannot be null");
        Objects.requireNonNull(executor, "executor cannot be null");

        FastTool adaptedTool = new FastTool() {
            @Override
            public String name() {
                return schema.name();
            }

            @Override
            public String description() {
                return schema.description();
            }

            @Override
            public FastObservation execute(Map<String, Object> args) {
                long start = System.nanoTime();
                try {
                    String output = executor.apply(args != null ? args : Collections.emptyMap());
                    return ToolExecutionResult.ok(schema.name(), output, System.nanoTime() - start);
                } catch (Throwable t) {
                    return ToolExecutionResult.error(schema.name(), "Execution failed: " + t.getMessage(), System.nanoTime() - start);
                }
            }
        };

        tools.put(schema.name(), adaptedTool);
        mcpSchemas.put(schema.name(), schema);
        return this;
    }

    /**
     * Binds an event topic on the SidecarEventBus to trigger tool execution automatically.
     */
    public ToolBindingBridge bindEventTrigger(String topicPattern, String toolName, Function<SidecarEvent, Map<String, Object>> argumentExtractor) {
        if (eventBus == null) {
            throw(new IllegalStateException("SidecarEventBus is not configured for this ToolBindingBridge"));
        }
        eventBus.subscribe(topicPattern, event -> {
            Map<String, Object> args = argumentExtractor != null ? argumentExtractor.apply(event) : Collections.emptyMap();
            execute(toolName, args);
        });
        return this;
    }

    /**
     * Executes a registered tool by name with audit logging and event dispatch.
     */
    public FastObservation execute(String toolName, Map<String, Object> args) {
        Objects.requireNonNull(toolName, "toolName cannot be null");
        FastTool tool = tools.get(toolName);
        if (tool == null) {
            return ToolExecutionResult.error(toolName, "Tool '" + toolName + "' not found", 0);
        }

        Map<String, Object> safeArgs = args != null ? args : Collections.emptyMap();

        if (eventBus != null) {
            eventBus.publish("fastintegrate.tool.call", toolName, Map.of(
                    "tool", toolName,
                    "arguments", safeArgs
            ));
        }

        long start = System.nanoTime();
        FastObservation observation;
        try {
            observation = tool.execute(safeArgs);
        } catch (Throwable t) {
            long duration = System.nanoTime() - start;
            observation = ToolExecutionResult.error(toolName, "Unhandled error: " + t.getMessage(), duration);
        }
        long duration = System.nanoTime() - start;

        if (eventBus != null) {
            String resultTopic = observation.success() ? "fastintegrate.tool.result" : "fastintegrate.tool.error";
            eventBus.publish(resultTopic, toolName, Map.of(
                    "tool", toolName,
                    "success", observation.success(),
                    "message", observation.message() != null ? observation.message() : "",
                    "durationNs", duration
            ));
        }

        return observation;
    }

    public boolean hasTool(String toolName) {
        return tools.containsKey(toolName);
    }

    public FastTool getTool(String toolName) {
        return tools.get(toolName);
    }

    public McpToolDefinition getMcpSchema(String toolName) {
        return mcpSchemas.get(toolName);
    }

    public List<McpToolDefinition> exportMcpTools() {
        return List.copyOf(mcpSchemas.values());
    }

    public Set<String> toolNames() {
        return Collections.unmodifiableSet(tools.keySet());
    }

    public int toolCount() {
        return tools.size();
    }
}
