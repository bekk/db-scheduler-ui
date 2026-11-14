# Instance panel — single-instance detail (implementation spec)

A slide-over drawer showing **one execution** in full: status, why it's broken, payload, last
exception, recent history, and per-instance actions.

Eventually a child of the **task-detail** drill-down (planned spec), opening for the row the
operator selects there. **Until that view exists it is attached to Overview rows that have
exactly one instance.** Builds on `../overview-tasks-table/spec.md` (status model) and reuses
the existing admin (`TaskAdminController`) plumbing.

## Presentation

**Slide-over overlay** (Chakra `Drawer`), chosen by prototyping against a docked side panel and
an anchored popover. The side panel is more comfortable above ~1100px but has to stack under the
list below that, putting the detail off-screen on a click; the popover covers the rows it is
anchored to, and makes one scroll container serve both the fact table and the stack trace. The
slide-over is the only one whose behaviour does not change with viewport width.

Visual styling follows the existing app.

## Interaction

- **Selection:** selecting an instance opens its detail — from the task-detail list once that
  exists, today from a single-instance Overview row; the selected row stays highlighted.
- **One instance at a time** — selecting another replaces the contents.
- **Dismiss** via an explicit close or `Esc`.
- **Deep-link the selection** via `?task=<taskName>`, so refresh, back/forward and shared links
  reopen it. (An instance parameter joins it when a list exists to open the panel from.)
- **List context** returns on dismiss; the drawer never reorders or disturbs the list.

## Information shown (priority order)

Ordered for an operator triaging a failure — identity and *why it's broken* first, payload
and exception next, history and actions last.

| #  | Field                              | Content                                                                                         | Source                       | Gating           |
|----|------------------------------------|-------------------------------------------------------------------------------------------------|------------------------------|------------------|
| 1  | **Instance id**                    | `taskInstance` (full) + a short/elided form as the title                                        | `ScheduledExecution`         | —                |
| 2  | **Status**                         | Failed / Running / Scheduled (+ **overdue** flag)                                               | derived (below)              | —                |
| 3  | **Consecutive failures**           | count — the headline "how broken"; emphasize when `> 0`                                         | `consecutiveFailures`        | —                |
| 4  | **Next execution**                 | `executionTime`; for a picked row this is firing time (proxy); past + unpicked ⇒ **overdue**    | `executionTime`, `picked`    | —                |
| 5  | **Last failure**                   | timestamp, or `never`                                                                           | `lastFailure` (per-instance) | —                |
| 6  | **Last success**                   | timestamp, or `never`                                                                           | `lastSuccess` (per-instance) | —                |
| 7  | **Picked / Picked by**             | currently running? on which scheduler node                                                      | `picked`, `pickedBy`         | —                |
| 8  | **Task data**                      | the payload, pretty-printed JSON; `no task data` when null                                      | `taskData`                   | —                |
| 9  | **Last exception**                 | `exceptionClass` + `exceptionMessage` + stack trace (scrollable, monospaced)                    | `LogModel` (history)         | **history=true** |
| 10 | **Recent history (this instance)** | last N runs: outcome · `timeStarted` · `durationMs` · `exceptionMessage`; `View full history →` | `LogModel` (history)         | **history=true** |

**Status derivation (single instance):** `picked` ⇒ Running; else `consecutiveFailures > 0`
⇒ Failed; else Scheduled. An instance is **never dormant** (dormant is a task-level state —
a task with 0 instances has no drawer). A Scheduled instance with `executionTime` in the
past and not picked is **overdue** (warning), consistent with the Overview Next-run column.

## Data sourcing & availability (the load-bearing part)

- **Per-instance fields come from the individual `ScheduledExecution`.** db-scheduler exposes
  per execution: `taskInstance`, `taskData`, `executionTime`, `picked`, `pickedBy`,
  `lastSuccess`, `lastFailure`, `consecutiveFailures`, `version`.
- **Do not source these from the grouped `TaskModel`.** That model is group-shaped (one per
  task name, parallel arrays), and `lastFailure` / `lastHeartbeat` / `version` are
  **group-level scalars** — wrong or unavailable for a single instance.
- **Heartbeat is not available.** db-scheduler's `ScheduledExecution` exposes no heartbeat;
  `TaskModel.lastHeartbeat` is declared but never populated. **Omit the Heartbeat row** until
  core surfaces it — see `../future_features.md` cleanup.
- **Neither is version.** `ScheduledExecution` has no accessor for it either, so `TaskMapper`
  hardcodes `TaskModel.version` to `0`. **Omit the Version row** on the same grounds.
- **Served by its own endpoint**: `GET /db-scheduler-api/tasks/instance`,
  alongside `/tasks/overview` and gated by the same `db-scheduler-ui.overview` flag. It does
  **not** reuse `TaskLogic`/`LogLogic`, which are the previous generation and are slated for
  replacement:
  - the execution comes from `SchedulerClient#getScheduledExecution(TaskInstanceId)` — one row
    by primary key, where `TaskLogic` loads *every* scheduled execution per call and filters in
    Java;
  - the log side is a new `InstanceLogRepository` — one indexed query for one instance, no
    cache, no paging, and the stack-trace column read only for the single failed row shown;
  - `history` comes back `null` when `db-scheduler-ui.history` is off, so the client needs no
    config flag to know whether to render the section.
  - `id` is optional: without it the server resolves the task's sole execution (**404** none,
    **409** several), which is how the Overview opens the panel — its rows name a task, not an
    instance.
- **Exception detail and recent history come from the log table (`LogModel`)** — the
  scheduled-tasks row has no stack trace, only `consecutiveFailures` + the `lastFailure`
  timestamp. `LogModel` carries `exceptionClass`, `exceptionMessage`, `exceptionStackTrace`,
  `timeStarted`, `timeFinished`, `succeeded`, `durationMs`.

## Exception section (history-gated)

- Show the most recent **failed** log row for this instance: class, message, full stack
  (scrollable / monospaced).
- **`history=false`** ⇒ no stack is available. Show only `consecutiveFailures` + the
  `lastFailure` timestamp, plus a one-line hint that enabling `db-scheduler-ui.history`
  surfaces the exception and run history.

## Recent history — this instance (history-gated)

- Last N log rows for this instance, in the `history` block of the `GET /tasks/instance`
  response (see *Served by its own endpoint* above) — one indexed query on
  `(task_name, task_instance)`, not a filtered `/logs/all` page. Each line: outcome
  (ok/failed) · `timeStarted` · `durationMs` · `exceptionMessage`.
- `View full history →` deep-links the **History** page pre-filtered to this instance.
- **`history=false`** ⇒ hide the whole section.

## Actions

At the foot of the detail. All live under `TaskAdminController`, which is **absent when
`read-only=true`** (`@ConditionalOnProperty read-only=false`).

| Action        | Wiring                                                                           | Notes                 |
|---------------|----------------------------------------------------------------------------------|-----------------------|
| **Rerun now** | `POST /db-scheduler-api/tasks/rerun` (`id`=instance, `name`, `scheduleTime`=now) | exists                |
| **Delete**    | `POST /db-scheduler-api/tasks/delete` (`id`, `name`)                             | destructive ⇒ confirm |

- **Read-only:** when `read-only=true`, **hide all action buttons**; the drawer is then a
  pure detail view (consistent with the existing UI hiding `TaskRunButton`/`DotButton`).
- **Running instance:** disable destructive/rerun actions while `picked` (the current UI
  already hides delete for a running task).
- **After an action:** refetch the affected instance + the list; on **delete**, dismiss the
  detail.

> **Reschedule is intentionally absent** — there is no reschedule endpoint in the current
> version, so it's out of scope here (`TaskAdminController` has only rerun/rerunGroup/delete).

## Graceful degradation

- **`read-only=true`** ⇒ no action buttons.
- **`history=false`** ⇒ hide Last exception (stack) and Recent history; keep status, counts,
  and timestamps, plus the enable-history hint.
- **Heartbeat unavailable** ⇒ omit the row.
- **`taskData` null** ⇒ `no task data`.

## Relevant existing code

- `…/ui/model/TaskModel.java` — group-shaped; **do not** read per-instance
  `lastFailure`/`lastHeartbeat`/`version` from it.
- `…/ui/model/LogModel.java` — `exceptionClass` / `exceptionMessage` /
  `exceptionStackTrace` + per-run fields (exception + recent-history source).
- `…/ui/service/LogLogic.java`, `…/ui/controller/LogController.java` —
  `GET /logs/all`, `/logs/poll`. **Not used by the panel** (see *Served by its own endpoint*);
  listed because the `View full history →` link deep-links to the History page they serve.
- `…/ui/controller/TaskAdminController.java` — `/rerun`, `/delete` (read-only gated);
  **no `/reschedule`**. Service: `…/ui/service/TaskLogic.java` (`runTaskNow`, `deleteTask`).
- `…/ui/controller/ConfigController.java` — `/config` history + read-only flags.
- `src/utils/determineStatus.ts` — status set `['Failed','Running','Scheduled','Group']`;
  `Group` is removed by the redesign; an instance status ∈ {Failed, Running, Scheduled}.
- `src/components/scheduled/TaskAccordionButton.tsx`, `TaskAccordionItem.tsx` — the inline
  accordion this drawer **replaces**; `TaskRunButton` (rerun) + `DotButton` (delete) are the
  existing action controls.
- `src/utils/dateFormatText.ts` — relative/absolute timestamps.
- `src/components/overview/InstanceDrawer.tsx` — the drawer itself, with
  `src/hooks/useInstanceDetail.ts` and `src/hooks/useSelectedInstance.ts`.

## Out of scope

- Styling / visual design (follows the existing app).
- The instance **list**, status **tabs**, and keyset pagination — the **task-detail** spec.
- Editing the task payload.
- Populating `lastHeartbeat` (`../future_features.md` cleanup).
- **Reschedule** — no backend in the current version; not part of this work.
