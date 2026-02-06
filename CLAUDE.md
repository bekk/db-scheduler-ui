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

# Format Java code (Google Java Format via Spotless — required before PR)
./mvnw spotless:apply

# Sort pom.xml files
./mvnw sortpom:sort

# Check license headers
./mvnw license:check

# Add missing license headers
./mvnw license:format

# Frontend dev server (proxies API to localhost:8081)
cd db-scheduler-ui-frontend && npm run dev

# Frontend lint
cd db-scheduler-ui-frontend && npm run lint
```

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

- The frontend build is triggered during Maven's `generate-resources` phase via `exec-maven-plugin` (runs `npm install` and `npm run build` in `db-scheduler-ui-frontend/`), then output is copied to backend resources via `maven-resources-plugin`.
- Controllers are not annotated with `@Component` — they're instantiated as `@Bean` in `UiApiAutoConfiguration` so auto-configuration controls their lifecycle.
- Java code uses Lombok (`@Data`, `@Builder`, etc.) and Google Java Format style.
- All `.java`, `.ts`, `.tsx` source files (except tests) require Apache 2.0 license headers (enforced by `license-maven-plugin`).
- CI tests against Spring Boot 3.3, 3.4, 3.5, and 4.0 for compatibility.
- Frontend uses path alias `src` → `/src` (configured in vite.config.ts).
