# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A UI dashboard extension for [db-scheduler](https://github.com/kagkarlsson/db-scheduler) — a Java library for persistent task scheduling. The UI provides monitoring and administration (view, run, delete, reschedule tasks) via a Spring Boot auto-configured web interface.

## Build & Development Commands

```bash
# Full build (compiles frontend + backend, runs tests, checks license headers)
./mvnw clean install -q

# Run tests only
./mvnw test -q

# Run a single test class
./mvnw test -pl example-app -am -q -DskipTests && ./mvnw test -pl example-app -q -Dtest="SmokeTest"

# Fast feedback loop — skips frontend (pnpm install/build) and example-app* smoke tests.
# Use while iterating; see "Verification ladder" below for when to escalate.
./mvnw test -Pfast -q

# Format Java code (Google Java Format via Spotless — required before PR)
./mvnw spotless:apply

# Sort pom.xml files
./mvnw sortpom:sort

# Check license headers
./mvnw license:check

# Add missing license headers
./mvnw license:format

# Frontend dev server (proxies API to localhost:8081)
cd db-scheduler-ui-frontend && pnpm run dev

# Frontend lint
cd db-scheduler-ui-frontend && pnpm run lint
```

Tests are safe to run in a Docker-less environment: this project does not use Testcontainers. All integration tests run against in-memory H2 (`jdbc:h2:mem:...;MODE=PostgreSQL`), so `./mvnw test` and `./mvnw clean install` execute the full test suite without any external services.

## Verification ladder

A full `./mvnw test` takes ~2m30s cold. Most of that is the frontend pnpm build (~60s cold, ~15s warm), the recursive `license:check` walking the whole tree (~30s), and three `example-app*` Spring Boot smoke tests (~50s combined, one fresh context per test class). No test fetches any frontend asset — smoke tests only hit `/db-scheduler-api/...` — so skipping the frontend is safe for verification. Use the cheapest level that still covers the code you touched, and escalate before opening a PR.

| Level | Command | Cold time | Use when |
|---|---|---|---|
| `-Pfast` | `./mvnw -T 2C test -Pfast -q` | ~8s | Default fast loop. Runs library + starter unit tests; skips frontend, license check, and example-app smoke tests. |
| Skip-frontend, all tests | `./mvnw -T 2C test -Dexec.skip=true -q` | ~1m10s | Touched starter wiring, controllers, or anything the smoke tests exercise |
| Full clean build | `./mvnw clean install -q` | ~2m30s | Pre-PR; also runs spotless, license check, frontend build, and packages JARs |

Notes:
- `-Pfast` is defined in the root pom. It sets `exec.skip=true` (skips `pnpm install` + `pnpm run build`), sets `license.skip=true`, and activates a matching profile in each `example-app*` pom that sets `skipTests=true`.
- `-T 2C` runs modules in parallel; harmless to always include.
- If a change touches `db-scheduler-ui-frontend/`, drop `-Pfast` so the frontend actually rebuilds.
- Always run `./mvnw spotless:apply` and `./mvnw license:format` before pushing — the full build fails without them.

## Module Architecture

Multi-module Maven project (`pom.xml` at root):

- **db-scheduler-ui** — Core library. REST controllers (`/db-scheduler-api/**`), service logic (`TaskLogic`, `LogLogic`), query/filter/sort utilities, and task-to-model mapping. Contains the bundled frontend in `src/main/resources/static/`. No Spring Boot auto-configuration here — just plain Spring `@RestController` beans.
  - `TaskController` (GET endpoints: `/all`, `/details`, `/poll`)
  - `TaskAdminController` (POST endpoints: `/rerun`, `/rerunGroup`, `/delete`) — disabled when `read-only=true`
  - `LogController` — task execution history (requires `history=true`)
  - `ConfigController` — exposes UI config (history enabled, read-only mode)
  - `Caching` — in-memory cache of scheduler executions for polling/status changes

- **db-scheduler-ui-starter** — Spring Boot 3 auto-configuration (`UiApiAutoConfiguration`). Wires up all beans from `db-scheduler-ui`, handles context-path/servlet-path resolution, SPA fallback routing, and index.html rewriting. Properties class: `DbSchedulerUiProperties`.

- **db-scheduler-ui-spring-boot-4-starter** — Spring Boot 4 variant of the starter. Near-identical to the Boot 3 starter (only `WebMvcRegistrations` import differs due to Boot 4 package relocation).

- **db-scheduler-ui-frontend** — React/TypeScript SPA (Vite + Chakra UI + React Query). Built output is copied into `db-scheduler-ui/src/main/resources/static/db-scheduler/` during `mvn install`. Dev server runs on port 51373 and proxies `/db-scheduler-api` to `localhost:8081`.

- **example-app** — Spring Boot app with H2, sample tasks, and integration tests (smoke tests, read-only, Spring Security, context-path). Main class: `ExampleApp`. Runs on port 8081.

- **example-app-boot4** — Spring Boot 4 variant of the example app (uses Jackson 3/`tools.jackson` packages, `@AutoConfigureTestRestTemplate`, Boot 4 starter renames).

- **example-app-webflux** — WebFlux variant of the example app.

## Key Patterns

- The frontend build is triggered during Maven's `generate-resources` phase via `exec-maven-plugin` (runs `pnpm install --frozen-lockfile` and `pnpm run build` in `db-scheduler-ui-frontend/`), then output is copied to backend resources via `maven-resources-plugin`. The pnpm version is pinned via the `packageManager` field in `package.json`.
- Controllers are not annotated with `@Component` — they're instantiated as `@Bean` in `UiApiAutoConfiguration` so auto-configuration controls their lifecycle.
- Java code uses Lombok (`@Data`, `@Builder`, etc.) and Google Java Format style.
- All `.java`, `.ts`, `.tsx` source files (except tests) require Apache 2.0 license headers (enforced by `license-maven-plugin`).
- CI tests against Spring Boot 3.4, 3.5, and 4.0 for compatibility.
- Frontend uses path alias `src` → `/src` (configured in vite.config.ts).
