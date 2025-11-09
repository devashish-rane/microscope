# microscope
Dev-side observability for Spring Web (DebugFlow MVP)

Modules
- `core` (`debugflow-core`): Spring Boot auto-config + AOP for method-level tracing, runtime session toggle, console JSON exporter.
- `target_spring_web_project`: Simple CRUD app to test/verify instrumentation.

Build & Run (requires internet for Maven deps)
- In repo root (`microscope`): `mvn -q -DskipTests install`
- Run the sample app: `mvn -q -pl target_spring_web_project spring-boot:run`
- Enable a debug session: `curl -X POST http://localhost:8081/api/session/enable -H 'Content-Type: application/json' -d '{"ttlMinutes":20}'`
- Exercise CRUD:
  - `curl -X POST http://localhost:8081/users -H 'Content-Type: application/json' -d '{"name":"Alice","email":"a@x.com"}'`
  - `curl http://localhost:8081/users`
  - `curl http://localhost:8081/users/1`

Expected
- App logs include lines prefixed with `[DebugFlow]` containing FLOW events as JSON.
- TTL auto-expires (default 20m) or use `POST /api/session/disable`.
