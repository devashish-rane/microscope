DebugFlow Core (MVP)

What’s here
- Spring Boot auto-config that wires:
  - Session API: `/api/session` (enable/disable + TTL)
  - TraceContextFilter: sets/propagates `traceparent` + MDC `traceId`
  - FlowAspect: method-level timing for `@RestController`, `@Service`, `@Repository`
  - Console exporter: emits JSON to logs for each FLOW event

How to use (within parent repo)
- Add module dependency on `debugflow-core`
- Run the target app and call `POST /api/session/enable` to start a 20m session
- Call your app endpoints; watch `[DebugFlow] { ... }` JSON in logs

Notes
- This MVP focuses on runtime toggling and method-level flow export.
- SQL capture, UI, and WS are intentionally deferred for next increments.

