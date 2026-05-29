# MVP 1 — Overview page (implementation spec)

Task-centric landing dashboard: **one row per task name** (not per execution) so an
operator sees the health of every task at a glance. Companion mockup:
`screenshots/01-overview.png` (source sketch `design.excalidraw`). Deferred items:
`../future_features.md`.

## Navigation

- New **Overview** tab, nav order `Overview | Scheduled | History`.
- **Overview is the default landing page.**
- **Scheduled** = unchanged flat per-execution list; it is the drill-down target of the
  `→ list` and `→ instance` link. 
- **History** unchanged.

## Page structure

```
All tasks                                          ← H1
TASK                  NEXT RUN     LAST RUN         ← column headers
RECURRING · 5                                       ← section header + count
● <name>              <relative>   <last run>   →   ← row
  <status> [for <duration>]                         ← sub-line
ONE-TIME / CUSTOM · 4
● <name>              <relative>   <last run>   →
```

- Two sections: **RECURRING** and **ONE-TIME / CUSTOM**, each with a `· N` count.
- **Strictly alphabetical** within each section. Stable positions — never reorder by status.
- **No pagination** — render all rows (bounded by task-definition count, not executions).
- **No search / filter controls** and **no action buttons** (Run/Rerun/Delete) in this MVP.
  Navigation-only ⇒ inherently safe under `read-only=true`.

## Row data

Each row aggregates all instances of one task name:

| Element           | Content                                                                         |
|-------------------|---------------------------------------------------------------------------------|
| **Status marker** | Colored dot **+ one-word label**. Color is never the only signal (a11y).        |
| **Task name**     | Bold.                                                                           |
| **Sub-line**      | `<status word> [for <duration>] [· instance summary]` — see rules below.        |
| **Next run**      | Relative time to soonest next execution; hover = absolute timestamp.            |
| **Last run**      | Most-recent last-success / last-failure across instances; hover = absolute.     |
| **Link**          | `→ instance` (1 instance) or `→ list` (many → Scheduled filtered by task name). |

### Status (dot + label)

Severity order **failing > running > scheduled > dormant**. Multi-instance row shows the
**worst** state present.

| State     | Condition                                        |
|-----------|--------------------------------------------------|
| failing   | any instance `consecutiveFailures > 0`           |
| running   | any instance `picked == true` (and none failing) |
| scheduled | ≥1 scheduled instance, none failing/running      |
| dormant   | **one-time/custom** task definition with **0** scheduled instances |

> Recurring tasks are **never dormant** — a recurring task always reschedules itself, so a
> registered recurring definition always has ≥1 scheduled instance. (A recurring task with 0
> scheduled instances is an error/abnormal state, not a normal "dormant" row — out of scope here.)

### Sub-line rules

- **failing:** `failing for <duration>`, duration = `now − lastSuccess` (how long broken).
  Never succeeded (`lastSuccess` null) ⇒ plain `failing`, no duration.
- **running:** `running for <duration>`, duration = `now − executionTime` of the picked
  execution. This is a **proxy** for run time (core keeps `execution_time` at firing time
  while picked). The NEXT RUN column still shows `running now`; the duration lives only here.
- **scheduled / dormant:** status word only.
- **Instance count:** recurring rows **omit** it (one schedule ⇒ `· 1 instance` is noise).
  One-time/custom rows **append** `· N instance(s)`.
- **Multi-instance** (one-time/custom): worst-status dot + per-state breakdown instead of a
  single duration, e.g. `100 instances · 3 failing · 2 running · 95 scheduled`.

### Next-run column

- Scheduled, future ⇒ relative (`in 1h`, `in 26m`).
- Running (`picked`) ⇒ `running now` (no duration here — it's in the sub-line).
- Overdue (scheduled time in past, not picked) ⇒ `due 3h ago` in a **warning** color.
- Dormant ⇒ `—`.
- Multi-instance ⇒ soonest (min) across instances.

### Last-run column

- Most-recent `lastSuccess` and/or `lastFailure` across instances (failure red, success green).
- Relative format, consistent with Next-run; hover = absolute.
- **No `history=true` needed** — from `ScheduledExecution.getLastSuccess()/getLastFailure()`,
  not the log table.
- Never run (no success and no failure) ⇒ muted `never run`.

## Backend

### New Overview endpoint

`GET /db-scheduler-api/tasks/overview` — does group-by + aggregation **server-side**. Do
**not** patch legacy `TaskMapper.groupTasks` (latent bug: its `lastFailure` is the *first*
non-null instance, not the most recent). One object per task name, suggestion:

```
OverviewTask {
  taskName: string
  recurring: boolean | null      // null = could not determine (degraded mode)
  instanceCount: int
  counts: { failing: int, running: int, scheduled: int }
  worstStatus: "FAILING" | "RUNNING" | "SCHEDULED" | "DORMANT"
  nextExecutionTime: Instant | null   // soonest across instances
  lastSuccess: Instant | null         // most-recent across instances
  lastFailure: Instant | null         // most-recent across instances
  maxConsecutiveFailures: int
}
```

Time-in-state durations need **no extra fields** — derived client-side (*failing for X* =
`now − lastSuccess`; *running for X* = `now − nextExecutionTime` for a picked single-schedule row).

### Aggregation (new `SchedulerClient` group-by method — in MVP 1)

Aggregate by grouping by task name **in the database**, not by fetching every
`ScheduledExecution` and grouping in Java (today's `Caching.java` / `TaskMapper.groupTasks`).

- Returns per-task-name: `instanceCount`, `counts`, soonest `nextExecutionTime`, most-recent
  `lastSuccess`/`lastFailure`, `maxConsecutiveFailures`.
- `SchedulerClient` lives in **db-scheduler core**; repo tracks `db-scheduler:master-SNAPSHOT`,
  so add the method upstream.
- Per-execution fields available: `taskInstance`, `executionTime`, `picked`, `pickedBy`,
  `lastSuccess`, `lastFailure`, `consecutiveFailures`.
- `recurring` flag and dormant rows are **not** from this DB aggregation — they overlay from
  registered task definitions (below).

### Recurring detection

A task name is recurring iff its registered definition is `instanceof RecurringTask`
(`com.github.kagkarlsson.scheduler.task.helper.RecurringTask`). Inject registered `Task<?>`
beans into `UiApiAutoConfiguration` to build the recurring-name set.

- Prefer this over the `taskInstance == "recurring"` string heuristic (wrong for
  multi-instance recurring, `RecurringTaskWithPersistentSchedule`, or custom instance ids).

### Dormant tasks

Show **one-time/custom** task definitions with 0 scheduled executions, flagged `dormant`.
Requires the registered task list (same source as recurring detection). Recurring definitions
are **never** shown as dormant (see Status table note).

### Graceful degradation

The UI may run against only a `SchedulerClient` with **no task definitions**. It must not crash:
fall back to `recurring = null` and **omit dormant rows** (the DB group-by still yields the full
aggregated list). Today the code requires a full `Scheduler` — don't regress that, but make the
task-definition lookup optional.

## Relevant existing code

- Model: `db-scheduler-ui/src/main/java/no/bekk/dbscheduler/ui/model/TaskModel.java`
- Grouping: `…/ui/util/mapper/TaskMapper.java` (`groupTasks`)
- Service: `…/ui/service/TaskLogic.java`
- Filter/sort: `…/ui/util/QueryUtils.java`
- Execution fetch/cache: `…/ui/util/Caching.java`
- Config flags: `…/ui/controller/ConfigController.java`
- Bean wiring: `db-scheduler-ui-starter/…/autoconfigure/UiApiAutoConfiguration.java`
- Frontend status logic: `db-scheduler-ui-frontend/src/utils/determineStatus.ts`
- Frontend page / cards: `…/src/pages/FrontPage.tsx`, `…/src/components/scheduled/*`
- Relative-time util: `…/src/utils/dateFormatText.ts`

## Out of scope (deferred — see `future_features.md`)

Global summary bar, quick-filter chips, task-name search, `lastHeartbeat` cleanup, and precise
core-sourced run-duration. (The `SchedulerClient` group-by method **is** in MVP 1.)
