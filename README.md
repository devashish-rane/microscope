# microscope
Dev-side observability for Spring Web (DebugFlow MVP)

Modules
- `core` (`debugflow-core`): Spring Boot auto-config + AOP for method-level tracing, runtime session toggle, console JSON exporter.
- `target_spring_web_project`: Simple CRUD app to test/verify instrumentation.

Build & Run (requires internet for Maven deps)
- In repo root (`microscope`): `mvn -q -DskipTests install`
- Start both services: `make start-all`
- Sessions auto-enable with no expiry (see `application.yml`). No POST required.
- Exercise endpoints:
  - CRUD: `make create-user`, `make list-users`
  - Scenarios: `make s-chain3`, `make s-fanout`, `make s-callable`, `make s-exception`, `make s-async`
  - Cross-service: `make s-xsvc`
- Tail combined log (both services): `make flow-log-all`
- Filter by trace id (first 8 hex): `make flow-trace TID=<id>`

Expected
- Pretty flow lines for method START/END with durations, HTTP status, and SQL lines (with rows and timing) merged into `debugflow-all.log`.
Install in another Spring Boot project
- Add dependency (after running `mvn install` in this repo):
  - Maven:
    - pom.xml: `<dependency><groupId>com.debugflow</groupId><artifactId>debugflow-core</artifactId><version>0.1.0-SNAPSHOT</version></dependency>`
  - Gradle (from Maven local): `implementation("com.debugflow:debugflow-core:0.1.0-SNAPSHOT")`
  - Or drop-in JAR: build `core` and add `core/target/debugflow-core-0.1.0-SNAPSHOT.jar` to your app classpath.
- Minimal config in `application.yml`:
  - `spring.application.name: my-service`
  - `debugflow.enabled: true` (auto-on)
  - `debugflow.ttlMinutes: -1` (infinite)
  - `debugflow.consolePretty: true`
  - `debugflow.prettyFile: debugflow.log` (or a shared path, e.g. `../debugflow-all.log` to merge logs across services on the same host)
  - Optional: `debugflow.showThread: true`, `debugflow.showHttp: true`, `debugflow.micros: true`, `debugflow.followInboundTraces: true`
- Use
  - Start your app; no POST needed. For timeboxed sessions, call `POST /api/session/enable` with `{ "ttlMinutes": 20 }`.
  - Outbound `RestTemplate` calls propagate `traceparent` and emit HTTP client spans.
  - Async (`@Async`, `Callable`) keeps trace via MDC task decorator (auto-configured).
  - To capture SQL timings with rows, optionally wrap `JdbcTemplate` with a bean like `SqlCaptureTemplate` (see `target_orders_service`).

Notes
- Infinite session default is implemented via `debugflow.ttlMinutes: -1`.
- To disable, call `POST /api/session/disable` or set `debugflow.enabled: false`.
