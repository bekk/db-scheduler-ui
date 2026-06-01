# Overview page — implementation notes & decision log

Companion to `spec.md`. Records the design decisions reached in two grilling sessions and gives
a clear overview of what was built. Implemented on branch `overview-page` (2026-06-01).

The feature is a task-centric landing dashboard — **one row per task name** — gated behind the
non-public flag `db-scheduler-ui.overview` (default **off**).

---

## 1. How we got here — the grilling sessions

The plan was stress-tested question-by-question before and during implementation. Two sessions:

### Session A — walking the design tree (15 decisions)

Each decision below was resolved one at a time, with the recommended option confirmed by the user.

| # | Decision | Outcome |
|---|----------|---------|
| 1 | **Scope** | Implement phases 1–3 first; phase 4 (default landing) as a follow-up. *(Later all 4 done.)* |
| 2 | **Backend layout** | Reuse `TaskLogic` (`getOverview`) + `TaskController` (`/overview`) + a **new** `OverviewMapper` — do not touch the latent-bug `TaskMapper.groupTasks`. |
| 3 | **Task-def injection / degraded mode** | Inject `List<Task<?>>` into `TaskLogic`; **empty list ⇒ degraded** (`recurring=null`, no dormant rows). `Scheduler` stays required. |
| 4 | **Response shape** | Flat, name-sorted `List<OverviewTask>`; the frontend groups into sections and sorts. |
| 5 | **Degraded-mode rendering** | Single unsectioned list; instance count shown on every row. |
| 6 | **`counts` bucketing** | Mutually exclusive by worst state (`failing > running > scheduled`); a picked-and-failing execution counts as **failing**. Buckets sum to `instanceCount`. |
| 7 | **`nextExecutionTime` (spec contradiction)** | Defined as soonest **non-picked** execution (drives the NEXT RUN column correctly, incl. multi-instance). Added one extra field **`runningSince`** (soonest picked firing time) to power the `running for X` sub-line — a deliberate, minimal deviation from the spec's "no extra fields" line, which broke the multi-instance case. |
| 8 | **Relative-time formatter** | The spec's "reuse `dateFormatText.ts`" was a gap (it is absolute-only). Added a **new compact `relativeTimeText`** (s/m/h/d) to that same file; kept `dateFormatText` for absolute hover tooltips. |
| 9 | **Drill-down links** | `→ instance` (single) and `→ list` (many) both navigate to `/{taskName}` (Scheduled filtered by name); label-only difference. Dormant rows: no link. |
| 10 | **Toggle wiring / gating** | Flag flows properties → `ConfigResponse.showOverview` → `getShowOverview` → TopBar/FrontPage. **UI-only gating** — the endpoint is always present on `TaskController`; only the tab/route/landing are gated. |
| 11 | **Example apps** | `overview=true` in all three example apps; no fabricated demo tasks. |
| 12 | **Tests** | `OverviewMapper` + `TaskLogic` unit tests, an example-app smoke test, config on/off tests. Frontend = lint + build only (no FE test framework exists). |
| 13 | **Recurring detection** | *(superseded in Session B — see below)* |
| 14 | **Refresh** | Fetch-on-mount + a manual Refresh button (`refetch`); **no auto-poll** (the global 2s `refetchInterval` is overridden to `false`). |
| 15 | **NEXT RUN column decision tree** | dormant → `—`; `nextExecutionTime==null` → `running now`; future → `in X` / `soonest in X`; past → `due X ago` / `soonest due X ago` (warning). `soonest ` prefix only when multi-instance. |

### Session B — the `RecurringTaskWithPersistentSchedule` correction

The biggest correction. Session A (Q13) had chosen a **dual** `instanceof` check
(`RecurringTask || RecurringTaskWithPersistentSchedule`). That was wrong.

Bytecode (db-scheduler 15.6.0) settled it:

- `RecurringTask implements OnStartup` → auto-creates its execution at startup → always ≥1
  scheduled instance → a perpetual **singleton**, never dormant.
- `RecurringTaskWithPersistentSchedule` does **not** implement `OnStartup` → never auto-schedules.
  Its schedule lives in `task_data` (dynamic), one definition backs 0..N instances, and 0
  instances is genuinely **dormant**.

So it behaves like the one-time/custom family, not a fixed recurring singleton. **Decisions:**

- Recurring predicate = **`instanceof RecurringTask` only**.
- Second section renamed `ONE-TIME / CUSTOM` → **`ONE-TIME, DYNAMIC & CUSTOM`** (it holds
  one-time, custom, and dynamic/persistent-schedule tasks).
- `spec.md` and `future_features.md` were updated to match; the spec's own note that had listed
  `RecurringTaskWithPersistentSchedule` as a should-be-recurring case was corrected.

Verified live: the example app's `dynamic-recurring-task` (a registered-but-unscheduled
`RecurringTaskWithPersistentSchedule`) returns `recurring=false`, `worstStatus=DORMANT`.

---

## 2. What changed — overview of the implementation

Delivered in four phases, each merging without changing existing behavior (flag off by default).

### Phase 1 — toggle + empty flag-gated page

| File | Change |
|------|--------|
| `db-scheduler-ui/.../model/ConfigResponse.java` | Added `showOverview` field (+ `@NoArgsConstructor`). |
| `db-scheduler-ui/.../controller/ConfigController.java` | Added `showOverview` constructor arg, returned in `/config`. |
| `db-scheduler-ui-starter` + `…-spring-boot-4-starter` `DbSchedulerUiProperties.java` | Added `overview` (default `false`). |
| both `UiApiAutoConfiguration.java` | `configController` bean passes `properties.isOverview()`. |
| `frontend/src/utils/config.ts` | `getShowOverview()`. |
| `frontend/src/components/common/TopBar.tsx` | Nav bar renders when `showOverview || showHistory`; new **Overview** tab (order Overview \| Scheduled \| History). |
| `frontend/src/pages/FrontPage.tsx` | Flag-gated `/overview` route. |
| `frontend/src/pages/OverviewPage.tsx` | Page component (placeholder in P1, filled in P3). |
| 3× `example-app*/application.properties` | `db-scheduler-ui.overview=true`. |
| `example-app/.../ConfigOverviewOnTest.java`, `ConfigOverviewOffTest.java` | On/off flag coverage. |

### Phase 2 — backend endpoint + aggregation

| File | Change |
|------|--------|
| `db-scheduler-ui/.../model/WorstStatus.java` | New enum `FAILING/RUNNING/SCHEDULED/DORMANT`. |
| `db-scheduler-ui/.../model/OverviewCounts.java` | New `{failing, running, scheduled}`. |
| `db-scheduler-ui/.../model/OverviewTask.java` | New row model (incl. extra `runningSince`). |
| `db-scheduler-ui/.../util/mapper/OverviewMapper.java` | New clean group-by; dedupes the picked-duplication from `Caching`; non-picked `nextExecutionTime`; picked `runningSince`; most-recent last-success/failure. |
| `db-scheduler-ui/.../service/TaskLogic.java` | New `getOverview(refresh)`; new `List<Task<?>>` constructor (old kept as delegating overload); recurring set via `instanceof RecurringTask`; dormant overlay; degraded mode. |
| `db-scheduler-ui/.../controller/TaskController.java` | `GET /db-scheduler-api/tasks/overview?refresh=`. |
| both `UiApiAutoConfiguration.java` | `taskLogic` bean injects `List<Task<?>>`. |
| `…/util/mapper/OverviewMapperTest.java`, `…/service/TaskLogicOverviewTest.java`, `example-app/.../OverviewSmokeTest.java` | Unit + smoke tests. |

### Phase 3 — frontend two-section table

| File | Change |
|------|--------|
| `frontend/src/models/OverviewTask.ts` | TS shape mirroring the backend. |
| `frontend/src/services/getOverview.ts` | Fetch `/tasks/overview` + query key. |
| `frontend/src/utils/dateFormatText.ts` | Added compact `relativeTimeText` (s/m/h/d). |
| `frontend/src/utils/overviewFormat.ts` | Pure formatters: section label, status dot/tint, sub-line, NEXT/LAST RUN cells, drill-down label. |
| `frontend/src/pages/OverviewPage.tsx` | Full page: two-section table (degraded → single list), status dot + name + sub-line, next/last-run with absolute hover, drill-down, manual refresh (auto-poll disabled). |

### Phase 4 — Overview as default landing (flag-gated)

| File | Change |
|------|--------|
| `frontend/src/pages/FrontPage.tsx` | Flag on: index `/` redirects to `/overview`; Scheduled flat list moves to `/scheduled`. Flag off: unchanged. Static routes ranked above the `/:taskName` catch-all. |
| `frontend/src/components/common/TopBar.tsx` | Scheduled button targets `/scheduled` when the flag is on. |

### Docs

`spec.md`, `future_features.md` (derived-requirements log) updated for the Session B correction.

---

## 3. Verification

- **Backend unit/smoke:** `OverviewMapperTest` (5), `TaskLogicOverviewTest` (2),
  `ConfigOverviewOnTest`/`OffTest`, `OverviewSmokeTest` — green; existing suites unaffected.
- **Frontend:** `pnpm run lint` + `pnpm run build` (tsc + vite) — clean.
- **Live end-to-end** (example-app + Vite dev server): `/config` reports `showOverview:true`;
  `/tasks/overview` returns correct data for every branch — recurring classification (incl.
  `dynamic-recurring-task` → `recurring=false`/`DORMANT`), dormant rows, a running instance
  (`runningSince` set, `nextExecutionTime=null` → "running now"), and a 100-instance task.

**Caveat:** the build sandbox has **no headless browser**, so there is no literal screenshot —
visual rendering was verified via type-checking + the live JSON contract. A human eyeball on the
rendered page (colours/spacing vs. the mockup) is still worth doing locally.

---

## 4. Follow-ups / deferred

- **Pre-PR gate:** run `./mvnw clean install` (spotless, license, frontend bundle, all smoke tests).
- **DB-side group-by:** MVP aggregates in Java; the per-task-name `SchedulerClient` group-by belongs
  upstream in db-scheduler core — swap in once released (transparent, same contract).
- Header summary strip + filter chips are a separate spec (`../overview-header-summary/spec.md`).
