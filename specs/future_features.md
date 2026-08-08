# Overview — future features & derived requirements

Backlog for the Overview page beyond MVP 1 (see `MVP_1_overview_spec.md`). This is a
**living document** — append requirements here as we discover them while building the
current feature. Keep MVP 1 lean; park anything non-essential below.

---

## Header++ : global summary bar

> **Moved to its own spec** (`overview-header-summary/spec.md`) — 2026-05-28. The global
> summary strip (N tasks · N failing · N running · N scheduled · history-gated last-hour
> throughput) is now specced there, together with the quick-filter chips below.

## Quick-filter chips

> **Moved to `overview-header-summary/spec.md`** — 2026-05-28. Specced as three
> AND-combined client-side toggles (Has failures / Running now / Recurring only); the
> summary-strip counts double as click-to-filter shortcuts for them.

## Task-name search box

Search/filter the Overview by task name (the existing list has
`searchTermTaskName` + exact-match). Becomes more important as the number of task
definitions (and dormant rows) grows.

> **Moved to MVP 1.** The `SchedulerClient` DB-side group-by method is part of the
> current feature — see `MVP_1_overview_spec.md` §5.

## Summary-strip cards: single-select or AND-combined?

**Reported 2026-08-07.** Selecting *Failing* while *Scheduled* is active leaves both on and
filters to their intersection; the expectation was that the second selection replaces the
first, the way a segmented control or a tab bar behaves.

The current behaviour is what `overview-header-summary/spec.md` specifies — "three
**independent toggle** chips, **AND-combined**" — so this is a decision to revisit, not an
implementation slip. What changed is the presentation: the chips became stat cards, and
`Tasks` reads as "selected" whenever nothing else is, so the row now *looks* like a
one-of-N selector while behaving as N checkboxes.

Worth weighing before changing it:

- AND-combining answers real questions — *failing **and** has queued work* is a different
  set from either alone, and the `Showing X of Y tasks` line exists to make the combination
  legible.
- Single-select is what the cards' own visual language promises, and it makes `Tasks` a
  natural member of the set ("all") rather than a special case.
- A middle option: keep AND but make combination visible — e.g. only the active cards
  outlined and an explicit `+` between them — so multi-select stops looking accidental.

Whichever way it goes, `Tasks` and the other cards should follow one rule; today `Tasks`
clears everything while the rest toggle.

## Legacy log queries cannot use the log table's indexes

**Found 2026-08-08**, while adding `stl_task_instance_idx` for the instance panel.

`QueryUtils.logSearchCondition` builds every task-name and task-instance filter as
`LOWER(task_name) = LOWER(:term)` (`QueryUtils.java:146-150`), and a plain b-tree index
cannot be seeked through a function-wrapped column. So the History page's filters — including
the per-instance one the ⋮ menu deep-links to — scan the log table.

Measured on H2 (`MODE=PostgreSQL`, 5 000 rows, `explain analyze`):

| Query shape                                   | Plan                      | Rows scanned |
|-----------------------------------------------|---------------------------|--------------|
| `task_name = ? and task_instance = ?`         | `stl_task_instance_idx`   | **2**        |
| `LOWER(task_name) = ? and LOWER(task_instance) = ?` | table scan          | 5 001        |
| `LOWER(task_name) = ?`                        | names `stl_task_name_idx` | 5 001        |

The third row is the point: the index appears in the plan but is read as a scan, not a seek.
As far as we can tell **no current UI query can seek `stl_task_name_idx`** — every
`task_name` filter in `LogLogic` goes through `logSearchCondition` — leaving `stl_started_idx`
(time-range) as the only index doing work.

Caveats: measured on H2 only. MySQL with a case-insensitive collation, or Postgres with a
`lower(task_name)` functional index, would behave differently, and this does not prove the
History page is slow in production — only that the index cannot serve that query shape.

The fix is not another index; it is to stop wrapping the column. When `/logs/all` is replaced,
pick one: compare the columns directly (what `/tasks/instance` does), add functional indexes on
`lower(...)`, or give the columns a case-insensitive collation. Note that dropping `LOWER`
changes search semantics — today's exact-match search is case-insensitive — so that is a
product decision, not only a performance one.

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
- 2026-05-28 · global summary bar + quick-filter chips promoted out of this backlog into
  `overview-header-summary/spec.md`; task-name search box stays deferred here.
- 2026-05-29 · `instance-panel/spec.md` written. Discovered: (a) **Reschedule** has **no
  backend** — `TaskAdminController` only has rerun/rerunGroup/delete; **dropped for this work**
  (not in the current version); would need a new `POST /tasks/reschedule`
  (`SchedulerClient.reschedule`) + time-picker if revived later;
  (b) the instance **exception + stack trace** live only in the log table (`LogModel`), so the
  detail's exception/recent-history sections are **history-gated**; scheduled_tasks has none.
- 2026-05-29 · instance-panel **presentation form left open** in the spec (side panel /
  slide-over / popover / inline / dedicated route) — to be settled by prototyping variants;
  only the information set + behaviour are locked.
- 2026-08-07 · instance panel built as a **slide-over** (decided by prototyping the three
  candidates — see `instance-panel/spec.md` §Presentation). Attached to **single-instance
  Overview rows only**, since the Scheduled tab is being replaced; a task with one execution
  *is* that execution, so no list is needed in between. Discovered: (a) **version is not
  available** either (no accessor on `ScheduledExecution`; `TaskMapper` hardcodes `0`) —
  omitted like `lastHeartbeat`, and both should be removed from `TaskModel` in the same
  cleanup; (b) the panel got its **own endpoint** (`GET /tasks/instance`) rather than reusing
  `TaskLogic`/`LogLogic` — every `/tasks/details` call loads all scheduled executions and
  filters in Java, where `SchedulerClient#getScheduledExecution` is a primary-key lookup. This
  continues the pattern started by `/tasks/overview`: **new endpoints for the new UI, old ones
  retired once nothing calls them**. `/tasks/details` and `/logs/all` are now used only by the
  Scheduled and History pages;
  (c) `GET /logs/all` has an **inverted `asc` flag** — `LogLogic:138` maps `asc=true` to
  `id desc`. Not worth fixing in place given the endpoint is on its way out, but the
  replacement must not inherit it;
  (c2) **`SchedulerClient#getScheduledExecutionsForTask(taskName)` hides running executions** —
  the single-argument overload narrows to `picked=false` (`SchedulerClient:619`), so a task's
  only execution vanishes exactly while it runs. Pass `ScheduledExecutionsFilter.all()`
  explicitly. The instance list will hit the same trap;
  (c3) the log table needs an index on `(task_name, task_instance, id)` for per-instance
  reads — added to `sql/log-table/*.sql` and the example-app migrations, but **existing
  deployments must add it by hand**;
- 2026-08-07 · summary-strip cards read as a one-of-N selector but behave as AND-combined
  toggles (above). Surfaced in use, not in review — the spec sanctions the behaviour, so
  only trying it caught the mismatch.
- 2026-08-08 · the legacy log queries `LOWER()`-wrap the columns they filter, so they cannot
  seek any index on `task_name` / `task_instance` (above). Came out of asking why the log
  table had no per-instance index: it had no query that could have used one.
  (d) **`rerun` clears** `lastSuccess`/`lastFailure`/`consecutiveFailures` (db-scheduler's
  `reschedule` resets execution state) — the panel makes this visible, so consider saying so
  in the button's copy.
