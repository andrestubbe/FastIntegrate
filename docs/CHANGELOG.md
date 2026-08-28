# Changelog

All notable changes to `FastIntegrate` will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.1.0] - 2026-08-28

### Added
- **Universal Sidecar EventBus**: High-throughput pub-sub engine with synchronous and asynchronous execution, hierarchical wildcard routing (`*` and `#`), and Dead Letter Queue (DLQ).
- **High-Throughput Webhook Router**: Zero-allocation path router with dynamic URL parameters, constant-time HMAC-SHA256 / SHA-1 signature validation, and auto-forwarding to EventBus topics.
- **FastAIRuntime / FastAIMCP Tool Binding Bridge**: Native `FastTool` registration, bi-directional `McpToolDefinition` export and adaptation, and reactive event triggers.
- **FastIntegrate Unified Facade**: Central builder for orchestrating EventBus, Router, and Tool Bridge.
- **FastANSI 120-Column Hero Demo**: Interactive showcase and CLI formatter with tree branching, bold metrics, and path truncation.
- **OpenJDK JMH Benchmark Suite**: Microbenchmarks measuring event dispatch, HMAC validation, webhook routing, and tool execution latency.
