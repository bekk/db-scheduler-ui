---
name: explore-ui
description: Launch the db-scheduler-ui in a browser to explore, screenshot, or verify UI changes. Use when running the app, taking screenshots of the dashboard, driving the UI with Playwright/Chromium, or confirming a frontend change works end-to-end. Covers both bundled-UI mode (one process) and Vite dev-server mode (frontend HMR).
---

# Explore the db-scheduler-ui in a browser

Two launch modes. Pick by whether you need frontend hot-reload.

## A. Backend only (bundled UI) — one process

Fastest. Use when you only need to view/drive the UI, not edit the frontend.

```bash
./mvnw install -DskipTests -q                # once, to bundle frontend into the backend JAR
(cd example-app && ../mvnw spring-boot:run)  # background; UI at http://localhost:8081/db-scheduler
```

- Trailing slash matters: `/db-scheduler` works, `/db-scheduler/` returns 404.
- The bundle is built at `mvn install` time, not live — if you edit `db-scheduler-ui-frontend/`, use mode B instead.

## B. Backend + Vite dev server — two processes

Required for frontend HMR. The dev server proxies `/db-scheduler-api/**` to the backend.

```bash
(cd example-app && ../mvnw spring-boot:run)    # background; backend on :8081
(cd db-scheduler-ui-frontend && pnpm run dev)  # background; dev server on :51373
```

Open `http://localhost:51373/`.

## Readiness signals

Wait for these before driving the UI — works with `until grep -q '...' <logfile>` or a Monitor:

- **Backend ready:** `Started ExampleApp in N seconds`
- **Dev server ready:** `VITE vX.Y.Z  ready in N ms` and `Local:   http://localhost:51373/`

Backend startup is ~3s, dev server ~1s after deps are installed.

## What you'll see

The example-app pre-registers a mix of recurring, failing, long-running, and group-spawning tasks (see `example-app/src/main/java/.../tasks/`). The landing page is non-empty within a few seconds of startup — no seeding needed for screenshots or smoke checks.

Key UI routes (both modes serve the same SPA):
- `/` — "Scheduled" tab: all tasks with status, next-execution time, run/rerun controls
- `/history/all` — "History" tab: execution log (requires `db-scheduler-ui.history=true`, which `example-app` sets)

**Flag-gated pages won't appear unless their flag is set** (e.g. History needs `db-scheduler-ui.history=true`; an in-progress feature behind its own toggle needs that toggle on). If a tab/route is missing, check the flag is enabled in the example-app's `application.properties` before assuming it's broken.

## Driving it

Whatever headless browser is available — Playwright MCP (`mcp__playwright__browser_*`), `chromium-cli`, raw `curl` for API smoke. Same URLs above.

Save screenshots under `.screenshots/` (gitignored) so they don't litter the repo root.

## Common pitfalls

- **404 at `/db-scheduler/`** (trailing slash): use `/db-scheduler` instead.
- **Bundled UI shows stale content**: re-run `./mvnw install -DskipTests -q` — the bundle is a build artifact.
- **API 404s like `/db-scheduler-api/all`**: paths are nested under resource, e.g. `/db-scheduler-api/tasks/all`, `/db-scheduler-api/logs/...`, `/db-scheduler-api/config`.
- **Port already in use**: backend is `${PORT:8081}` (see `example-app/src/main/resources/application.properties`); dev server port is hard-coded to `51373` in `vite.config.ts`.
