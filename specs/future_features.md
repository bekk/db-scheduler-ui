# Overview — future features & derived requirements

Backlog for the Overview page beyond MVP 1 (see `MVP_1_overview_spec.md`). This is a
**living document** — append requirements here as we discover them while building the
current feature. Keep MVP 1 lean; park anything non-essential below.

---

## Header++ : global summary bar

A summary strip on top of the Overview, above the task list.

Proposed content (left → right):
- `N tasks` — count of distinct task names.
- `N failing (M instances)` — tasks with ≥1 failing instance, and the total failing
  instance count.
- `N running` — count of picked instances.
- `N total scheduled` — total scheduled instances.
- `last hour: N ok · M failed` — throughput.

Feasibility / data:
- All counts except throughput are **free** from the Overview endpoint's aggregation
  (no extra query, no history).
- `last hour: N ok · M failed` **requires `history=true`** (the log table, via
  `LogLogic`). Render this element **only when history is enabled**
  (`ConfigController` exposes the flag at `/db-scheduler-api/config`); hide it otherwise.

Interaction:
- Counts double as **click-to-filter** shortcuts (e.g. click "failing" → filter list to
  failing tasks). Depends on the filter chips below.

## Quick-filter chips

Toggle chips above the list:
- **Has failures** → existing `filter=FAILED`.
- **Running now** → existing `filter=RUNNING`.
- **Recurring only** → the `recurring` flag from the new endpoint.

These map onto the existing filter params (`ALL | FAILED | RUNNING | SCHEDULED |
SUCCEEDED`) plus the new recurring flag. Cheap once the endpoint exposes `recurring`.

## Task-name search box

Search/filter the Overview by task name (the existing list has
`searchTermTaskName` + exact-match). Becomes more important as the number of task
definitions (and dormant rows) grows.

> **Moved to MVP 1.** The `SchedulerClient` DB-side group-by method is part of the
> current feature — see `MVP_1_overview_spec.md` §5.

## Cleanup: dead `lastHeartbeat` field

`TaskModel.lastHeartbeat` is declared but never populated (db-scheduler's
`ScheduledExecution` doesn't expose it). Either populate it (if a source becomes
available) or remove it.

## Possibly later

- **Precise run-duration for running tasks** — MVP 1 shows `running for <duration>` in the
  sub-line using `now − executionTime` (firing time) as a **proxy**, accurate only when the
  task was picked promptly. A *precise* run time needs a real start-time / heartbeat from
  db-scheduler core (`ScheduledExecution` exposes neither today); revisit when core surfaces
  it. (NEXT RUN column stays `running now`. See `MVP_1_overview_spec.md` §4 Sub-line rules.)
- **Per-section severity sort toggle** — MVP is strictly alphabetical; a "problems
  first" toggle could be offered later.
- **Dormant-recurring alerting** — a registered recurring task with 0 next executions
  is abnormal (failed to reschedule). Consider surfacing it more prominently than a
  plain dormant one-time task.

---

## Derived requirements log

Append items here as they come up during implementation (date · note):

- 2026-05-28 · (seed) doc created from the MVP 1 interview.
- 2026-05-28 · run-duration **proxy** (`now − executionTime`) pulled into MVP 1 for the
  `running for <duration>` sub-line; precise/core-sourced duration stays deferred (above).
