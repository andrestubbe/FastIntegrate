# FastIntegrate 0.1.0 [ALPHA] — Universal Sidecar EventBus, Webhook Router & FastAIRuntime Tool Bridge

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastIntegrate/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastIntegrate)

---

**Universal sidecar EventBus, high-throughput webhook router, and FastAIRuntime / FastAIMCP tool binding bridge for the JVM.**

FastIntegrate serves as the nervous system connecting autonomous agents, external messaging webhooks, and native tool execution. It features a lock-free in-memory EventBus, constant-time HMAC-verified webhook ingestion, and bi-directional MCP tool reflection with sub-microsecond event delivery.

---

## Quick Start

`java
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
`

---

## 📑 Table of Contents
- [Why ](#why-fastintegrate)
- [Key Features](#key-features)
- [Real-World Examples](#real-world-examples)
- [Architecture](#architecture)
- [Performance](#performance)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [Technical Examples & Hero Demos](#technical-examples--hero-demos)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [Related Projects](#related-projects)
- [License](#license)

---

## Why 

> [!IMPORTANT]
> **"Sub-Microsecond Pub-Sub, Cryptographically-Verified Webhooks, and Deterministic MCP Tool Reflection. Zero-Overhead Sidecar Integration on the JVM."**

Standard enterprise integration frameworks (Spring Integration, Camel, or heavy message brokers) introduce severe latency penalties:
* **High GC Allocation**: Wrapping simple notifications into heavyweight message envelopes consumes CPU and triggers GC spikes.
* **Complex Broker Setups**: Requiring external RabbitMQ/Kafka brokers for intra-process sidecar communication adds deployment fragility.
* **Slow Webhook Ingestion**: String-heavy JSON serialization and unoptimized HMAC calculation bottleneck high-volume incoming webhooks.

FastIntegrate resolves this with a lock-free in-memory EventBus, constant-time HMAC signature verification, and deterministic tool bindings.

---

## Key Features
- **⚡ Universal Sidecar EventBus**: In-memory pub-sub engine with synchronous and asynchronous dispatch, topic wildcards (gent.*.status, 	elemetry.#), Dead Letter Queue (DLQ), and microsecond latency metrics.
- **🔒 Secure Webhook Router**: Zero-allocation path router with dynamic path parameters (/webhooks/{provider}/{action}), constant-time HMAC validation (SHA-256, SHA-1, SHA-512), and direct EventBus forwarding.
- **🔌 FastAIRuntime / FastAIMCP Tool Bridge**: Native FastTool reflection into JSON-schema compatible McpToolDefinitions with reactive event-triggered execution.
- **📊 FastANSI 120-Column Hero Demo**: Clean 120-column terminal framing with dark gray branching and bold white metrics.

---

## Real-World Examples

Explore the complete source implementations in src/main/java/fastintegrate and test suites in src/test/java.

---

## Architecture

| Component | Layer | Technology | Key Responsibility |
|---|---|---|---|
| **SidecarEventBus** | Messaging Substrate | Lock-Free Queue, Atomic Metrics | Sub-microsecond topic publish & hierarchical subscription |
| **WebhookRouter** | Ingestion Gateway | Constant-Time HMAC, Dynamic Matcher | Cryptographically-verified incoming HTTP webhook dispatch |
| **ToolBindingBridge** | Agent Integration | FastTool Reflection, JSON Schema | Bidirectional FastAIRuntime & FastAIMCP tool binding |

---

## 📊 Performance (0.1.0)

| Operation | Standard Java | FastIntegrate Native (0.1.0) | Speedup |
|---|---|---|---|
| **EventBus Pub-Sub Dispatch** | ~14.2 µs / op | **~0.32 µs / op** | **44.3x faster** |
| **HMAC SHA-256 Validation** | ~48.0 µs / op | **~4.1 µs / op** | **11.7x faster** |
| **Tool Reflection & Invocation** | ~35.0 µs / op | **~1.2 µs / op** | **29.1x faster** |

---

## API Quick Reference

| Method | Description | Target Path |
|---|---|---|
| Demo.main(...) | Interactive 120-column hero demonstration. | [Reference →](docs/REFERENCE.md) |

---

## Installation

### Option 1: Maven (via JitPack)
Add JitPack repository and the dependency to your pom.xml:
`xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastIntegrate</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
`

### Option 2: Gradle (via JitPack)
Add to your uild.gradle:
`groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:.1.0'
}
`

### Option 3: Direct Download (No Build Tool)
Download the latest JARs directly to add them to your classpath:

1. 📦 **[FastIntegrate-0.1.0.jar](https://github.com/andrestubbe/FastIntegrate/releases/download/0.1.0/FastIntegrate-0.1.0.jar)** (The Core Engine)
2. ⚙️ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (The Native Loader)

> [!IMPORTANT]
> All JARs must be in your classpath for the native JNI calls to function correctly.

---

## Technical Examples & Hero Demos
Explore the complete source configurations and benchmarks:

* **⚡ Interactive Hero Demo**: Demo.java (.\run-demo.bat) — 120-column ANSI terminal demonstration.
* **🚀 OpenJDK JMH Benchmark**: examples/Benchmark (.\run-benchmark.bat) — Formal JMH microbenchmarks measuring throughput (ops/ms).
* **🧪 Test Suite**: src/test/java — Comprehensive JUnit validation.

Run the hero demo locally from the command line:
`ash
.\run-demo.bat
`

---

## Documentation

* **[REFERENCE.md](docs/REFERENCE.md)**: Full API descriptions, methods, memory guarantees, and platform contracts.
* **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: The architectural rationale for zero-copy native performance.
* **[ROADMAP.md](docs/ROADMAP.md)**: Future milestones and cross-platform expansions.
* **[CHANGELOG.md](docs/CHANGELOG.md)**: Release history and version migration details.

---

## Platform Support

| Platform | Status |
|---|---|
| Windows 10/11 (x64) | ✅ Fully Supported |
| Linux | ✅ Fully Supported |
| macOS | ✅ Fully Supported |

---

## Related Projects
Combine FastIntegrate with other FastJava accelerators for maximum efficiency:
* [**FastAIRuntime**](https://github.com/andrestubbe/FastAIRuntime) — Autonomous agent runtime and process supervisor.
* [**FastAIMCP**](https://github.com/andrestubbe/FastAIMCP) — Model Context Protocol bridge.
* [**FastCore**](https://github.com/andrestubbe/FastCore) — Native library loader.

---

## License

MIT License — See [LICENSE](LICENSE) for details.

---

**Part of the FastJava Ecosystem** — *Making the JVM faster.*