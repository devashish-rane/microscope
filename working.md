# Microscope workspace quick tour

## What this repo is
- Parent Maven project with three modules: `core` (DebugFlow library), `target_spring_web_project` (CRUD + demo scenarios on port 8081), and `target_orders_service` (orders API with Postgres/Testcontainers on port 8082).
- Goal: demonstrate dev-side observability for Spring Web apps via method-level tracing, HTTP/SQL capture, and W3C trace propagation.

## Build and run
- Build everything (needs internet for Maven deps): `mvn -q -DskipTests install`.
- Start both sample services: `make start-all` (or individually `make start` for the CRUD app and `make start-orders` for the Orders service). Stop with `make stop-all`.
- Exercise endpoints (port 8081): CRUD (`make create-user`, `make list-users`) and scenarios (`make s-chain3`, `make s-fanout`, `make s-callable`, `make s-exception`, `make s-async`, cross-service `make s-xsvc`).
- Observe flows: pretty merged log at `debugflow-all.log` (`make flow-log-all`) or per-app (`make flow-log`). Filter by trace prefix: `make flow-trace TID=<8hex>`.
- Session API auto-enabled (TTL = infinite) via `application.yml`. To toggle manually: `curl -X POST :8081/api/session/enable` (or `/disable`).

## Module map
- `core/` (`debugflow-core`): Spring Boot auto-config + AOP that powers DebugFlow.
  - Config/properties: `DebugFlowAutoConfiguration` wires everything; `DebugFlowProperties` exposes `debugflow.*` settings (enable flag, TTL, console/file outputs, HTTP/thread toggles, simple class names, inbound trace following).
  - Session control: `SessionManager` tracks active window with optional TTL; `SessionController` exposes `/api/session` endpoints.
  - Trace context: `TraceContextFilter` assigns/propagates W3C `traceparent`, sets MDC (`traceId`, `df.span`, `df.depth`), and echoes header back to clients.
  - Method tracing: `FlowAspect` wraps all `@RestController`, `@Service`, `@Repository` methods; emits START/END `TraceEvent` with depth, HTTP info, errors, and span/parent IDs. ThreadLocals + MDC keep nesting/async depth correct.
  - Async + propagation: `MdcTaskDecorator` preserves MDC across `@Async`/MVC async; `TracePropagation` customizes `RestTemplate` to forward `traceparent` and emit FLOW spans for outbound HTTP.
  - Export pipeline: `EventBus` fans events to `Exporter` implementations.
    - `ConsoleJsonExporter` logs JSON lines.
    - `PrettyConsoleExporter` prints colored single-line tree.
    - `FilePrettyExporter` appends pretty lines to a file (trace, service, op, duration, HTTP status, thread optional) and also renders SQL events if present.
  - IDs: `TraceIds` generates trace/span IDs; `SqlEvent` type exists so SQL timings can also be emitted.
- `target_spring_web_project/` (port 8081): test app using the core library.
  - Config: `application.yml` enables DebugFlow, pretty console + file output to `../debugflow-all.log`, shows HTTP + thread info.
  - CRUD: `UserController` + `UserService` + `UserRepository` (in-memory).
  - Scenarios in `scenarios/`: chain A→B→C, fanout/merge, MVC `Callable`, forced exception, and `@Async` tasks. `ScenarioController` also calls the Orders service via `RestTemplate` to show cross-service trace propagation.
  - Async wiring: `AsyncConfig` defines an executor with `TaskDecorator`; `WebMvcConfig` hooks it into Spring MVC async handling.
- `target_orders_service/` (port 8082): companion service to show SQL capture + cross-service tracing.
  - DB setup: `DbContainerConfig` spins up a Postgres Testcontainer + Hikari `DataSource` + `JdbcTemplate` when `orders.useTestcontainers=true` (default).
  - SQL capture: `SqlCaptureTemplate` wraps `JdbcTemplate`, times queries/updates, and publishes `SqlEvent` with rows count + depth (using MDC trace context). Wired via `SqlConfig`.
  - Domain: `OrderRepo` creates schema + seed rows, queries by `userId`; `OrderService` + `OrderController` expose `/orders?userId=`.

## How the instrumentation flows
- Incoming HTTP hits `TraceContextFilter`, which sets `traceId`/`traceparent` + MDC depth/span.
- `FlowAspect` surrounds controllers/services/repositories, publishing FLOW START/END events with nesting depth, durations, HTTP method/status, errors, and span/parent relationships.
- Async/parallel paths keep the trace via `MdcTaskDecorator` and depth tracking; RestTemplate calls use `TracePropagation` to forward `traceparent` and emit client-side spans.
- Orders service SQL calls emit `SqlEvent` so DB timing/row counts appear inline in the same trace tree.
- `EventBus` fans events to exporters so console/file logs show merged views; both apps point `debugflow.prettyFile` at `../debugflow-all.log` to coalesce flows.

## Handy commands while exploring
- `make start-all` then `make s-chain3` / `make s-fanout` / `make s-async` / `make s-xsvc`.
- Tail logs: `make flow-log-all` (merged pretty), or `make grep-flow` for JSON.
- Check session state: `make session-status` (CRUD) or `make enable-session-orders` (Orders).
