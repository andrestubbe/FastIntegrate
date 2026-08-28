# FastIntegrate Technical Reference

Complete API Reference and Architecture Specification for `FastIntegrate`.

---

## 1. Architecture Overview

```
 ┌──────────────────────────────────────────────────────────────────┐
 │                         FastIntegrate                            │
 ├────────────────────────────────┬─────────────────────────────────┤
 │     SidecarEventBus            │     WebhookRouter               │
 │ ├─ Topic Registry              │ ├─ Path Matcher ({provider})    │
 │ ├─ Hierarchical Wildcards (*,#)│ ├─ Constant-Time HMAC-SHA256    │
 │ ├─ Ring Buffer / Async Workers │ ├─ Auto-Forward to EventBus     │
 │ └─ Dead Letter Queue (DLQ)     │ └─ Response Pipeline            │
 ├────────────────────────────────┴─────────────────────────────────┤
 │                    ToolBindingBridge                             │
 │ ├─ FastAIRuntime (FastTool) Registration                         │
 │ ├─ FastAIMCP (McpToolDefinition) Adaptation                      │
 │ ├─ Reactive Event Topic Triggers                                 │
 │ └─ Audit Telemetry (fastintegrate.tool.call/result)             │
 └──────────────────────────────────────────────────────────────────┘
```

---

## 2. API Specification

### 2.1 SidecarEventBus
- `SidecarEventBus.create()`: Instantiates synchronous event bus.
- `SidecarEventBus.createAsync(workerThreads)`: Instantiates asynchronous worker-pool event bus.
- `subscribe(String topicPattern, EventSubscriber subscriber)`: Subscribes with wildcard matching (`agent.*.status`, `telemetry.#`).
- `publish(String topic, Object payload)`: Broadcasts event to all matching subscribers.
- `subscribeDeadLetter(EventSubscriber subscriber)`: Registers fallback handler for unrouted or failed events.
- `metrics()`: Returns real-time latency and throughput counters.

### 2.2 WebhookRouter
- `WebhookRouter.create(eventBus)`: Initializes router connected to optional SidecarEventBus.
- `post(String pathPattern, WebhookHandler handler)`: Registers POST route with `{param}` path variables.
- `postSecure(String path, String signatureHeader, String hmacSecret, WebhookHandler handler)`: Registers HMAC-protected endpoint.
- `forwardSecure(String path, String signatureHeader, String hmacSecret, String eventTopic)`: Authenticates and automatically emits payload to event bus.
- `dispatch(WebhookRequest request)`: Evaluates request and returns `WebhookResponse`.

### 2.3 ToolBindingBridge
- `registerTool(FastTool tool)`: Registers deterministic native tool.
- `registerMcpTool(McpToolDefinition schema, Function<Map, String> executor)`: Adapts external MCP tool into FastTool.
- `bindEventTrigger(String topicPattern, String toolName, Function extractor)`: Triggers tool execution upon event bus match.
- `execute(String toolName, Map<String, Object> args)`: Invocates tool with duration measurement and telemetry publishing.
- `exportMcpTools()`: Returns all tool definitions formatted for FastAIMCP protocol exchange.
