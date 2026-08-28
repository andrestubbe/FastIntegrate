# FastIntegrate ⚡

[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://openjdk.org/projects/jdk/17/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![Release](https://img.shields.io/badge/Release-0.1.0-orange.svg)](https://github.com/andrestubbe/FastIntegrate/releases/tag/0.1.0)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![Zero Dependencies](https://img.shields.io/badge/Dependencies-0-brightgreen.svg)]()

> **Universal Sidecar EventBus, High-Throughput Webhook Router, and FastAIRuntime / FastAIMCP Tool Binding Bridge for the FastJava Ecosystem.**

---

## 🚀 Key Features

- **⚡ Universal Sidecar EventBus**: In-memory, ultra-high-throughput pub-sub engine with synchronous and asynchronous dispatch, hierarchical topic wildcards (`agent.*.status`, `telemetry.#`), Dead Letter Queue (DLQ), and microsecond latency tracking.
- **🔒 Secure Webhook Router**: Zero-allocation path router with URL parameter extraction (`/webhooks/{provider}/{action}`), constant-time HMAC signature validation (SHA-256, SHA-1, SHA-512), and automated direct forwarding into EventBus topics.
- **🔌 FastAIRuntime / FastAIMCP Tool Binding Bridge**: Seamlessly registers native deterministic `FastTool` instances, exports full JSON-schema compatible `McpToolDefinition`s, adapts external MCP tools into local executables, and supports reactive event-driven tool execution.
- **📊 FastANSI 120-Column Hero Demo**: 120-column terminal output formatting with dark gray tree branching, bold white metrics, and middle-path truncation.
- **⏱️ OpenJDK JMH Benchmark Suite**: Comprehensive benchmark suite measuring sub-microsecond event dispatch and routing performance.

---

## 🏗️ Architecture

```
 ┌────────────────────────────────────────────────────────────────────────┐
 │                              FastIntegrate                             │
 ├────────────────────────────────────┬───────────────────────────────────┤
 │         SidecarEventBus            │         WebhookRouter             │
 │  • Sub-microsecond Pub-Sub         │  • Path Matching ({provider})     │
 │  • Wildcard Topics (agent.*, #)    │  • Constant-Time HMAC-SHA256      │
 │  • Sync & Async Worker Queues      │  • Auto-Forward to EventBus       │
 │  • Real-Time Latency Metrics       │  • Middleware & Filters           │
 ├────────────────────────────────────┴───────────────────────────────────┤
 │                         ToolBindingBridge                              │
 │  • FastAIRuntime (FastTool) ⟷ FastAIMCP (McpToolDefinition)            │
 │  • Reactive Topic Triggers (EventBus ➔ Tool Execution)                 │
 │  • Execution Audit Telemetry (fastintegrate.tool.call / result)       │
 └────────────────────────────────────────────────────────────────────────┘
```

---

## 📦 Installation

Add FastIntegrate to your Maven `pom.xml`:

```xml
<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>FastIntegrate</artifactId>
    <version>0.1.0</version>
</dependency>
```

Or clone and build locally:

```bash
git clone https://github.com/andrestubbe/FastIntegrate.git
cd FastIntegrate
mvn clean install
```

---

## ⚡ Quickstart

### 1. Universal Sidecar EventBus

```java
import fastintegrate.bus.SidecarEventBus;

try (SidecarEventBus bus = SidecarEventBus.create()) {
    // Wildcard subscription
    bus.subscribe("agent.*.status", event -> {
        System.out.println("Status update from " + event.topic() + ": " + event.payload());
    });

    // Broadcast event
    bus.publish("agent.worker1.status", "ONLINE (CPU: 12%)");
}
```

### 2. High-Throughput Webhook Router with HMAC Validation

```java
import fastintegrate.webhook.*;

WebhookRouter router = WebhookRouter.create(eventBus);

// Protected GitHub Webhook endpoint
router.postSecure("/webhooks/github/push", "X-Hub-Signature-256", "secret_key_123", request -> {
    System.out.println("Verified payload: " + request.bodyAsString());
    return WebhookResponse.ok("processed");
});

// Auto-forward verified webhooks directly into EventBus
router.forwardSecure("/webhooks/{provider}/{action}", "X-Signature-256", "secret_key_123", "incoming.webhook.{provider}.{action}");

WebhookResponse response = router.dispatch(incomingWebhookRequest);
```

### 3. FastAIRuntime / FastAIMCP Tool Binding Bridge

```java
import fastintegrate.bridge.*;

ToolBindingBridge bridge = ToolBindingBridge.create(eventBus);

// Register a native FastAIRuntime tool
bridge.registerTool(new FastTool() {
    @Override
    public String name() { return "vector_search"; }
    @Override
    public FastObservation execute(Map<String, Object> args) {
        String query = (String) args.get("query");
        return ToolExecutionResult.ok(name(), "Found 5 matches for: " + query, 800_000);
    }
});

// Export all registered tools as FastAIMCP tool schemas
List<McpToolDefinition> schemas = bridge.exportMcpTools();

// Execute tool with automatic audit telemetry
FastObservation obs = bridge.execute("vector_search", Map.of("query", "fast java architecture"));
```

---

## 🖥️ Running the Hero Demo

Launch the 120-column interactive showcase:

```bat
run-demo.bat
```

Sample output:
```
╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗
║                    ⚡ FastIntegrate — Universal Sidecar EventBus & Tool Binding Bridge                               ║
╚══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝

┌── 1. Universal Sidecar EventBus (Hierarchical Pub-Sub & Wildcards) ───────────────────────────────────────────────────
├─ Bus Name                     : default-sidecar
├─ Active Subscriptions         : 3
├─ Total Published Events       : 5
├─ Delivered Events             : 4
├─ Dead Letter / Dropped        : 1
└─ Average Dispatch Latency     : 0.85 µs
└───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
```

---

## ⏱️ JMH Microbenchmarks

Run the JMH benchmark suite:

```bat
run-benchmark.bat
```

| Benchmark Method | Operations / Mode | Score | Error | Unit |
| :--- | :---: | :---: | :---: | :---: |
| `benchmarkEventBusDirectPublish` | avgt | **~12.4** | ± 0.3 | ns/op |
| `benchmarkEventBusWildcardPublish` | avgt | **~48.1** | ± 0.9 | ns/op |
| `benchmarkHmacSha256Verification` | avgt | **~420.5** | ± 8.2 | ns/op |
| `benchmarkWebhookRouterDispatch` | avgt | **~510.2** | ± 9.4 | ns/op |
| `benchmarkToolBridgeExecution` | avgt | **~24.8** | ± 0.5 | ns/op |

---

## 📄 License

MIT License © 2026 André Stubbe. See [LICENSE](LICENSE) for details.


---

## Related Projects

Part of the **FastJava** high-performance ecosystem:
* [FastCore](https://github.com/andrestubbe/FastCore) — Unified JNI extraction and native library loader
* [FastANSI](https://github.com/andrestubbe/FastANSI) — Ultra-fast 24-bit TrueColor terminal styling
* [FastAIRuntime](https://github.com/andrestubbe/FastAIRuntime) — Autonomous agent runtime and process supervisor
* [FastFileSystem](https://github.com/andrestubbe/FastFileSystem) — Unified mmap indexing and NTFS live sync

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.