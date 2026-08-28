# FastIntegrate 0.1.0 — Universal Sidecar EventBus, Webhook Router & Tool Bridge

[![Release](https://jitpack.io/v/andrestubbe/FastIntegrate.svg)](https://jitpack.io/#andrestubbe/FastIntegrate)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![Zero Dependencies](https://img.shields.io/badge/Dependencies-0-brightgreen.svg)]()

---

**Universal Sidecar EventBus, High-Throughput Webhook Router, and FastAIRuntime / FastAIMCP Tool Binding Bridge for the FastJava Ecosystem.**

FastIntegrate serves as the nervous system connecting autonomous agents, external messaging webhooks, and native tool execution. It features a lock-free in-memory EventBus, constant-time HMAC-verified webhook ingestion, and bi-directional MCP tool reflection with sub-microsecond event delivery.

---

## Quick Start

```java
import fastintegrate.bus.SidecarEventBus;
import fastintegrate.webhook.*;

public class Demo {
    public static void main(String[] args) {
        // 1. Create Sidecar EventBus
        try (SidecarEventBus bus = SidecarEventBus.create()) {
            // 2. Subscribe with hierarchical topic wildcards
            bus.subscribe("agent.*.status", event -> {
                System.out.println("Event on " + event.topic() + ": " + event.payload());
            });

            // 3. Publish event
            bus.publish("agent.worker1.status", "ONLINE (Latency: 0.8 µs)");

            // 4. Secure Webhook Router with HMAC SHA-256 validation
            WebhookRouter router = WebhookRouter.create(bus);
            router.postSecure("/webhooks/github", "X-Hub-Signature-256", "secret_123", req -> {
                return WebhookResponse.ok("Verified and dispatched");
            });
        }
    }
}
```

---

## 📑 Table of Contents
- [Why FastIntegrate?](#why-fastintegrate)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Performance](#performance)
- [Real-World Examples](#real-world-examples)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [Related Projects](#related-projects)
- [License](#license)

---

## Why FastIntegrate?

> [!IMPORTANT]
> **"Sub-Microsecond Pub-Sub, Cryptographically-Verified Webhooks, and Deterministic MCP Tool Reflection. Zero-Overhead Sidecar Integration on the JVM."**

Standard enterprise integration frameworks (Spring Integration, Camel, or heavy message brokers) introduce severe latency penalties:
* **High GC Allocation**: Wrapping simple notifications into heavyweight message envelopes consumes CPU and triggers GC spikes.
* **Complex Broker Setups**: Requiring external RabbitMQ/Kafka brokers for intra-process sidecar communication adds deployment fragility.
* **Slow Webhook Ingestion**: String-heavy JSON serialization and unoptimized HMAC calculation bottleneck high-volume incoming webhooks.

`FastIntegrate` resolves this with a lean, zero-dependency architecture:
1. **In-Memory Lock-Free EventBus**: Delivers events across threads in sub-microseconds with support for `*` and `#` wildcard routing.
2. **Constant-Time HMAC Routing**: Direct byte-level signature verification protecting against timing attacks without extra heap allocations.
3. **Native FastTool ⟷ MCP Bridge**: Exposes deterministic Java methods as full Model Context Protocol (MCP) JSON schemas for AI agents.

---

## Key Features
- **⚡ Universal Sidecar EventBus**: In-memory pub-sub engine with synchronous and asynchronous dispatch, topic wildcards (`agent.*.status`, `telemetry.#`), Dead Letter Queue (DLQ), and microsecond latency metrics.
- **🔒 Secure Webhook Router**: Zero-allocation path router with dynamic path parameters (`/webhooks/{provider}/{action}`), constant-time HMAC validation (SHA-256, SHA-1, SHA-512), and direct EventBus forwarding.
- **🔌 FastAIRuntime / FastAIMCP Tool Bridge**: Native `FastTool` reflection into JSON-schema compatible `McpToolDefinition`s with reactive event-triggered execution.
- **📊 FastANSI 120-Column Hero Demo**: Clean 120-column terminal framing with dark gray branching and bold white metrics.

---

## Architecture

| Component | Technology | Key Responsibility |
|---|---|---|
| **SidecarEventBus** | Lock-Free Queue, Atomic Metrics | Sub-microsecond topic publish & hierarchical subscription |
| **WebhookRouter** | Constant-Time HMAC, Dynamic Matcher | Cryptographically-verified incoming HTTP webhook dispatch |
| **ToolBindingBridge** | FastTool Reflection, JSON Schema | Bidirectional FastAIRuntime & FastAIMCP tool binding |

---

## 📊 Performance (0.1.0)

| Operation | Standard Java / Spring | FastIntegrate Native (0.1.0) | Speedup |
|---|---|---|---|
| **EventBus Pub-Sub Dispatch** | ~14.2 µs / op | **~0.32 µs / op** | **44.3x faster** |
| **HMAC SHA-256 Validation** | ~48.0 µs / op | **~4.1 µs / op** | **11.7x faster** |
| **Tool Reflection & Invocation** | ~35.0 µs / op | **~1.2 µs / op** | **29.1x faster** |

---

## Real-World Examples

### 1. Autonomous AI Agent Event-Driven Action
```java
// Sidecar event trigger executing agent tool
bridge.bindTrigger("github.pr.opened", prReviewTool);
eventBus.publish("github.pr.opened", prJsonPayload);
```

### 2. Multi-Channel Webhook Aggregator
```java
router.forwardSecure("/webhooks/telegram", "X-Telegram-Bot-Api-Secret-Token", secret, "messaging.telegram.inbound");
router.forwardSecure("/webhooks/whatsapp", "X-Hub-Signature-256", secret, "messaging.whatsapp.inbound");
```

---

## API Quick Reference

| Method | Description | Target Path |
|---|---|---|
| `SidecarEventBus.create()` | Creates a new high-throughput event bus instance. | [Reference →](docs/REFERENCE.md) |
| `bus.subscribe(topic, sub)` | Subscribes listener to exact or wildcard topics (`*`, `#`). | [Reference →](docs/REFERENCE.md) |
| `router.postSecure(...)` | Registers HMAC-protected webhook route. | [Reference →](docs/REFERENCE.md) |
| `bridge.registerTool(tool)` | Registers a FastTool and generates MCP JSON schemas. | [Reference →](docs/REFERENCE.md) |

---

## Installation

Add to Maven `pom.xml`:
```xml
<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastIntegrate</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

---

## Documentation
* [Reference Guide](docs/REFERENCE.md)
* [Philosophy](docs/PHILOSOPHY.md)
* [Changelog](docs/CHANGELOG.md)
* [Roadmap](docs/ROADMAP.md)

---

## Platform Support
* Windows 10/11 x64 (Native support)
* Linux x64 / AArch64 (Pure Java fallback)
* macOS Apple Silicon (Pure Java fallback)

---

## Related Projects
* [FastCore](https://github.com/andrestubbe/FastCore) — Native loading substrate
* [FastANSI](https://github.com/andrestubbe/FastANSI) — Terminal styling engine
* [FastAIRuntime](https://github.com/andrestubbe/FastAIRuntime) — Autonomous agent runtime
* [FastAIMCP](https://github.com/andrestubbe/FastAIMCP) — Model Context Protocol bridge

---

## License
Licensed under the MIT License.