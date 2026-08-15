# Overview — future features & derived requirements

Backlog for the Overview page beyond MVP 1 (see `MVP_1_overview_spec.md`). This is a
**living document** — add a section here for anything we discover that still needs doing:
a deferred feature, a trap the next implementation will hit, a cleanup we owe. Decisions
already taken belong in the spec they were taken for, not here. Keep MVP 1 lean; park
anything non-essential below.

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

## Legacy log filters cannot seek the task-name / task-instance indexes

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

Dropping `LOWER` need not cost the user anything, though, if the field stops asking them to
type a name exactly: **autocomplete the task name from the names we already know**. Picking a
name from a list is both easier than typing one and exact by construction, which is what lets
the query compare the column directly.

No new endpoint is needed — `/tasks/overview` already returns every registered task — but the
candidates are *not* in the client yet where the search box lives: `OVERVIEW_TASKS_QUERY_KEY`
is queried only by `OverviewPage`, while History runs `ALL_LOG_QUERY_KEY` and its header
carries a bare text input. So this costs either the same query issued from History, or a
store both pages share. Worth deciding when `/logs/all` is replaced, not before.

## Existing deployments need the per-instance log index by hand

`stl_task_instance_idx` on `(task_name, task_instance, id)` went into `sql/log-table/*.sql`
and the example-app migrations with the instance panel, but those files are the *initial*
schema — nothing replays them for a database that already exists. Without the index, opening
the panel scans every log row belonging to the task. Needs a line in the release notes, or a
migration path if one is ever added.

## Reschedule has no backend

`TaskAdminController` has rerun / rerunGroup / delete and nothing else, so the instance panel
ships without a Reschedule action. Reviving it needs `POST /tasks/reschedule` over
`SchedulerClient.reschedule`, plus a time picker in the panel.

## Rerun silently clears the failure history

db-scheduler's `reschedule` resets execution state, so pressing **Rerun** wipes `lastSuccess`,
`lastFailure` and `consecutiveFailures`. The instance panel puts those fields next to the
button that destroys them — the copy should say so.

## Retire the legacy task and log endpoints

`/tasks/overview` and `/tasks/instance` set the pattern: **new endpoints for the new UI, old
ones retired once nothing calls them**. `/tasks/details` and `/logs/all` are now reached only
by the Scheduled and History pages, and both are being replaced. Two things the replacements
must not inherit:

- `GET /logs/all` has an **inverted `asc` flag** — `LogLogic:138` maps `asc=true` to `id desc`.
  Not worth fixing in place on an endpoint this close to deletion.
- `/tasks/details` loads every scheduled execution and filters in Java, where
  `SchedulerClient#getScheduledExecution` is a primary-key lookup.

## Trap: `getScheduledExecutionsForTask(taskName)` hides running executions

The single-argument overload narrows to `picked=false` (`SchedulerClient:619`), so a task's
only execution disappears from the result exactly while it runs. Pass
`ScheduledExecutionsFilter.all()` explicitly. `InstanceService` does; the instance list, when
it is built, will meet the same trap.

## Read the log table with db-scheduler's `JdbcRunner`, not spring-jdbc

`InstanceLogRepository` uses `NamedParameterJdbcTemplate`; `JdbcRunner` would drop the
spring-jdbc dependency and keep the UI on the same JDBC layer as the scheduler itself.
Blocked upstream: db-scheduler's parent pom still relocates `com.github.kagkarlsson.jdbc` to
`com.github.kagkarlsson.shaded.jdbc` at package time, left over from before `8c1e4fa`
("Inline micro jdbc", 2025-04-23) made that package first-party source. Until a release ships
without the relocation, the only importable name is the shaded one — and code compiled against
it breaks at runtime the moment the relocation goes. Revisit when it does; the README's minimum
db-scheduler version moves with it.

`JdbcCustomization` (`com.github.kagkarlsson.scheduler.jdbc`, unshaded, already used by
`JdbcLogRepository`) is available today and is the right way to read `time_started` back: the
column is written with `setInstant`, whose UTC handling `getTimestamp` does not mirror.

## Cleanup: dead `lastHeartbeat` and `version` fields

Neither is populated: db-scheduler's `ScheduledExecution` exposes no accessor for the heartbeat,
and `TaskMapper` hardcodes `version` to `0`. Populate them if a source appears, or remove both
from `TaskModel` in one pass.

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
