# Instance panel — single-instance detail (implementation spec)

The side drawer in the task drill-down that shows **one execution** in full: status, why
it's broken, payload, last exception, recent history, and per-instance actions. Mockups of
the three candidate presentation forms: `screenshots/01-side-panel.png` (docked side panel),
`screenshots/02-slide-over.png` (slide-over overlay), `screenshots/03-popover.png` (floating
popover).

Child of the **task-detail** drill-down (planned spec) — it opens for the row the operator
selects in that view. Builds on `../overview-tasks-table/spec.md` (status model) and reuses
the existing history (`LogModel`) and admin (`TaskAdminController`) plumbing.

> **Focus: information + data sourcing + interaction.** Both the **visual styling** and the
> **panel's presentation form** are open for prototyping (see *Presentation* below) — this
> spec locks the *content* and *behaviour*, not the rendering.

## Interaction (presentation-agnostic)

- **Selection:** selecting an instance in the task-detail list opens its detail; the
  selected row stays highlighted.
- **One instance at a time** — selecting another replaces the contents.
- **Dismiss** via an explicit close / `Esc` (and, where it fits the form, re-clicking the
  selected row).
- **Deep-link the selection** via the URL (e.g. `?instance=<taskInstance>`) so refresh,
  back/forward, and shared links reopen it.
- **List context stays reachable** — exactly how depends on the presentation form (below).

## Presentation — open for prototyping

The *content* and *behaviour* above are fixed; **how the detail is rendered is not** — settle
it by prototyping a few variants. The three mockup screenshots above show the **docked
side-panel**, **slide-over**, and **popover** candidates — none is the decision yet.

| Variant | Sketch | Strengths | Weaknesses |
|---|---|---|---|
| **Docked side panel** | list left, detail pinned right (`screenshots/01-side-panel.png`) | compare instances; list stays visible; room for stack traces | needs width; cramped on narrow screens |
| **Slide-over overlay** | detail slides in over the list, dims the rest (`screenshots/02-slide-over.png`) | works on narrow screens; standard Chakra `Drawer` | hides the list while open |
| **Floating / hover popover** | small panel anchored to the clicked row (`screenshots/03-popover.png`) | lightweight peek; fast scan | too small for stack traces / payload; awkward to pin |
| **Inline row expansion** | row expands in place (today's accordion) | familiar; no new layout | pushes rows down; poor for long traces / comparing |
| **Dedicated route/page** | navigate to `/…/instance/<id>` | best for deep links + lots of content | loses list context; heavier nav |

Any variant must satisfy these invariants (the contract a prototype is judged against):

- Surfaces the **full information set** below — especially a possibly-long **stack trace**,
  without breaking layout (i.e. it scrolls).
- **One instance at a time**; easy to **dismiss** and to **switch** to another row.
- Selection is **deep-linkable** (URL reflects the open instance).
- **List context stays reachable** (visible alongside, or one back-action away).
- Does **not reorder or disturb** the list.
- Respects **read-only** (no actions) and **history gating**.

> Soft lean: docked side-panel on wide screens, slide-over on narrow — but prototype before locking.

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
| 8  | **Version**                        | optimistic-lock version (per-instance)                                                          | `version` (per-instance)     | —                |
| 9  | **Task data**                      | the payload, pretty-printed JSON; `no task data` when null                                      | `taskData`                   | —                |
| 10 | **Last exception**                 | `exceptionClass` + `exceptionMessage` + stack trace                                             | `LogModel` (history)         | **history=true** |
| 11 | **Recent history (this instance)** | last N runs: outcome · `timeStarted` · `durationMs` · `exceptionMessage`; `View full history →` | `LogModel` (history)         | **history=true** |

**Status derivation (single instance):** `picked` ⇒ Running; else `consecutiveFailures > 0`
⇒ Failed; else Scheduled. An instance is **never dormant** (dormant is a task-level state —
a task with 0 instances has no drawer). A Scheduled instance with `executionTime` in the
past and not picked is **overdue** (warning), consistent with the Overview Next-run column.

## Data sourcing & availability (the load-bearing part)

- **Per-instance fields come from the individual `ScheduledExecution`**, which the
  task-detail list already loads per row — **the drawer reuses that row's data**, no extra
  scheduled-tasks fetch. db-scheduler exposes per execution: `taskInstance`, `taskData`,
  `executionTime`, `picked`, `pickedBy`, `lastSuccess`, `lastFailure`,
  `consecutiveFailures`, `version`.
- **Do not source these from the grouped `TaskModel`.** That model is group-shaped (one per
  task name, parallel arrays), and `lastFailure` / `lastHeartbeat` / `version` are
  **group-level scalars** — wrong or unavailable for a single instance.
- **Heartbeat is not available.** db-scheduler's `ScheduledExecution` exposes no heartbeat;
  `TaskModel.lastHeartbeat` is declared but never populated. **Omit the Heartbeat row** (the
  mockup's `Heartbeat —` placeholder) until core surfaces it — see `../future_features.md`
  cleanup.
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

- Last N log rows for this instance via `GET /db-scheduler-api/logs/all` filtered by
  `taskName` **and** `taskInstance` (exact match — `searchTermTaskInstance` / `taskId`
  already support this). Each line: outcome (ok/failed) · `timeStarted` · `durationMs` ·
  `exceptionMessage`.
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
  `GET /logs/all`, `/logs/poll`; filter by `taskName` + `taskInstance` (exact).
- `…/ui/controller/TaskAdminController.java` — `/rerun`, `/delete` (read-only gated);
  **no `/reschedule`**. Service: `…/ui/service/TaskLogic.java` (`runTaskNow`, `deleteTask`).
- `…/ui/controller/ConfigController.java` — `/config` history + read-only flags.
- `src/utils/determineStatus.ts` — status set `['Failed','Running','Scheduled','Group']`;
  `Group` is removed by the redesign; an instance status ∈ {Failed, Running, Scheduled}.
- `src/components/scheduled/TaskAccordionButton.tsx`, `TaskAccordionItem.tsx` — the inline
  accordion this drawer **replaces**; `TaskRunButton` (rerun) + `DotButton` (delete) are the
  existing action controls.
- `src/utils/dateFormatText.ts` — relative/absolute timestamps.
- New component: the instance-detail component (form per *Presentation* — e.g. Chakra
  `Drawer` for a side/slide-over panel, a `Popover`, or a dedicated route) replacing the
  inline accordion expansion.

## Out of scope

- Styling / visual design (follows the existing app).
- The instance **list**, status **tabs**, and keyset pagination — the **task-detail** spec.
- Editing the task payload.
- Populating `lastHeartbeat` (`../future_features.md` cleanup).
- **Reschedule** — no backend in the current version; not part of this work.
