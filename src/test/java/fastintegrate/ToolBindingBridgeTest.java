package fastintegrate;

import fastintegrate.bridge.*;
import fastintegrate.bus.SidecarEvent;
import fastintegrate.bus.SidecarEventBus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ToolBindingBridgeTest {

    @Test
    public void testNativeToolRegistrationAndExecution() {
        SidecarEventBus bus = SidecarEventBus.create();
        ToolBindingBridge bridge = ToolBindingBridge.create(bus);

        FastTool calculatorTool = new FastTool() {
            @Override
            public String name() {
                return "calculator";
            }

            @Override
            public String description() {
                return "Performs arithmetic addition";
            }

            @Override
            public FastObservation execute(Map<String, Object> args) {
                int a = (int) args.getOrDefault("a", 0);
                int b = (int) args.getOrDefault("b", 0);
                return ToolExecutionResult.ok(name(), String.valueOf(a + b), 100);
            }
        };

        bridge.registerTool(calculatorTool);

        assertTrue(bridge.hasTool("calculator"));
        assertEquals(1, bridge.toolCount());

        FastObservation obs = bridge.execute("calculator", Map.of("a", 15, "b", 27));
        assertTrue(obs.success());
        assertEquals("42", obs.message());
        bus.close();
    }

    @Test
    public void testMcpToolAdapterAndSchemaExport() {
        ToolBindingBridge bridge = ToolBindingBridge.create();

        McpToolDefinition mcpDef = McpToolDefinition.of("weather_query", "Fetches current weather for city", Map.of(
                "type", "object",
                "properties", Map.of("city", Map.of("type", "string"))
        ));

        bridge.registerMcpTool(mcpDef, args -> {
            String city = (String) args.get("city");
            return "Weather for " + city + ": 22°C, Sunny";
        });

        assertEquals(1, bridge.exportMcpTools().size());
        assertEquals("weather_query", bridge.exportMcpTools().get(0).name());

        FastObservation result = bridge.execute("weather_query", Map.of("city", "Berlin"));
        assertTrue(result.success());
        assertTrue(result.message().contains("Berlin: 22°C"));
    }

    @Test
    public void testToolExecutionAuditEvents() {
        SidecarEventBus bus = SidecarEventBus.create();
        List<SidecarEvent> events = new ArrayList<>();
        bus.subscribe("fastintegrate.tool.#", events::add);

        ToolBindingBridge bridge = ToolBindingBridge.create(bus);
        bridge.registerTool(new FastTool() {
            @Override
            public String name() { return "echo_tool"; }
            @Override
            public FastObservation execute(Map<String, Object> args) {
                return ToolExecutionResult.ok("echo_tool", "ack", 50);
            }
        });

        bridge.execute("echo_tool", Map.of("text", "hello"));

        assertEquals(2, events.size());
        assertEquals("fastintegrate.tool.call", events.get(0).topic());
        assertEquals("fastintegrate.tool.result", events.get(1).topic());
        bus.close();
    }
}
