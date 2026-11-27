# Task detail — per-task instance list (implementation spec)

A page per task that lists its scheduled executions, filtered by status and paged through the
database, with the selected row expanding in place to the instance's details (the contents of
`../instance-panel/spec.md`, which first shipped as a side drawer).
It is the parent view the instance panel was always meant to have, and it replaces the legacy
`/scheduled/:taskName` drill-down for Overview users.

Builds on `../overview-tasks-table/spec.md` (status model, row wording) and
`../instance-panel/spec.md` (details, actions, history gating).

**Why inline, not a drawer:** user testing found the drawer far from the row on wide screens,
and the Overview confusing when one-instance rows opened a drawer while others opened a page.
Every Overview row now opens the task page, and details expand under their row.

## Who it is for

An **on-call operator triaging failures**: a task on the Overview is red, they click it and want
to see the failing instances first, open one, read the exception, and rerun. Checking a task's
backlog ("what is queued for the next hour?") is the secondary use and is served by the same
list.

## Scale

The page must stay usable when a single task has **millions of instances**. Assume nothing fits
in memory, on the server or in the browser:

- Every list request is a **keyset-paged query** bounded by the page size. No request loads a
  task's full execution set, and nothing goes through `TaskLogic` or `Caching`.
- Counts come from the per-task summary, never from counting loaded rows.
- The task-level action (rerun failed) streams through the database in batches and never
  materialises the set.

This is why the legacy drill-down (`GET /tasks/details`, which loads every execution in the
system and pages in Java) is replaced rather than extended.

## db-scheduler version

Every tab is a seeked query, because the failure-state filter now exists in db-scheduler:
`ScheduledExecutionsFilter.withFailing(boolean)` (db-scheduler #971) is
`coalesce(consecutive_failures, 0) > 0` / `= 0`, matching the `failing_count` /
`scheduled_count` columns of `getScheduledExecutionsSummaryByTask()`. Tab counts therefore equal
tab contents.

This raises the minimum db-scheduler version to the first release that contains it, a 17.x
release. Bump `db-scheduler.version` and `db-scheduler-boot4.version` in the root pom, and the
minimum version in the README.

**17.x migration (first commit).** db-scheduler 17 starters no longer register a default
`DbSchedulerCustomizer` bean (`UPGRADING.md`, "Upgrading to 17.x"). They read an optional
`DbSchedulerOverrides` or the deprecated `DbSchedulerCustomizer` through `ObjectProvider`. Both of
our starters inject `DbSchedulerCustomizer` directly (`UiApiAutoConfiguration`,
`DbSchedulerUiLogAutoConfiguration`) to find the scheduler's data source, so an application with
neither bean fails at startup. Resolve the data source the way the db-scheduler starter does:
overrides, then customizer, then the application's `DataSource`. Both of them are optional. Do
this in both starters, with a test for each case: no bean, overrides only, customizer only.

**Index recommendation.** A per-task, time-ordered page is fast only with an index on
`(task_name, execution_time, task_instance)`. The stock schema has only the primary key
`(task_name, task_instance)` and a global `execution_time_idx`, so without it a page either sorts
all of the task's rows or walks other tasks' rows in the time index. Recommend it in this
project's README and release notes, and add it to the example-app schema. Existing deployments add
it by hand, like `stl_task_instance_idx` (`../future_features.md`). Recommending it in
db-scheduler's own schema files is a separate upstream change.

**Overdue count.** `TaskSummary` has no overdue count, so the Overdue tab ships without a badge.
Adding one is an upstream change for later.

## Navigation

- **Route:** `/tasks/:taskName`, registered only when `db-scheduler-ui.overview` is on, like
  `/overview`. The task name is `encodeURIComponent`-encoded in the path.
- **Query string** (all optional, all restored on reload and back/forward):

  | Param         | Meaning                                                              |
  |---------------|----------------------------------------------------------------------|
  | `status`      | `all` · `failing` · `running` · `overdue` · `scheduled`             |
  | `from`        | ISO instant; the list starts at this execution time                  |
  | `instance`    | the instance expanded in the list (ids may contain `/ : space`, so never in the path) |
  | `autoRefresh` | same param and default (off) as the Overview (`useAutoRefresh`)     |

- **From the Overview:** every row with instances navigates to `/tasks/:taskName`, replacing
  today's `/scheduled/:taskName` target; its button reads **1 instance** or **N instances →**,
  and the row has "Opens the task page" as a tooltip. Dormant rows (0 instances) stay
  non-clickable.
- **One-instance tasks:** the page shows the header and the single row only (no tabs, toolbar
  or paging). The row is shown closed, then opens after ~150 ms with its details already
  fetched, so the reader sees the page they landed on become the details. Collapsing it keeps
  it closed.
- **Breadcrumb:** `Overview › <taskName>`. It returns to the Overview with its own query string
  (filters, auto-refresh) intact: link with the previous location when there is one, otherwise
  plain `/overview`.
- The legacy **Scheduled** tab and `/scheduled/:taskName` remain unchanged until the overview
  flag becomes default-on. Retiring them is not part of this spec.

## Page structure

Sketch (real screenshots replace it once built — see *Delivery*):

```
Overview › send-invoice-email                       ⟳ Auto-refresh   [Rerun all 12 failed…]
● send-invoice-email
  Failing for 2h · 1 204 311 instances · 12 failing · 3 running
  Next run in 2m · last run ✕ 4m ago ✓ 6m ago                         [Go to instance id…]
[All 1 204 311] [Failing 12] [Running 3] [Overdue 40] [Scheduled 1 204 256]   Start from [____] now −1h −1d earliest
INSTANCE                           STATUS     NEXT RUN      LAST RUN          FAILURES
invoice-2026-11-0088121            Failed     due 3m ago    ✕ 4m ago          3
{"customerId":4411,"amount":1…     (task-data preview, muted, one line)
…
                                   [ Load more ]
View history for this task →                               (history=true only)
```

### Header

Reuses the Overview row's wording for this task: status dot + label, the sub-line
(`failing for <duration>`, instance breakdown), next run and last run, following the same rules
as `../overview-tasks-table/spec.md` §Row data. Extract the wording helpers out of
`OverviewPage.tsx` into a shared module rather than duplicating them.

Source: the task's `OverviewTask` from `GET /tasks/overview`, under the same query key the Overview
uses. *(Builder's call: a single-task summary endpoint is a later optimisation; the all-tasks
summary is one `GROUP BY` and is already paid for by the Overview.)*

### Status tabs

The tabs are the Overview's stat cards (same look as the summary strip), single-select. As on the Overview, a card with a count of 0 is inert unless it is the current one (Overdue has no count and stays selectable). Each shows its count from the task summary; the header line above them carries timing only (one-time/recurring · next run · last run), so counts appear once.

| Tab       | Contents                                              | Filter                                        | Count           |
|-----------|-------------------------------------------------------|-----------------------------------------------|-----------------|
| All       | every execution                                       | none                                          | `instanceCount` |
| Failing   | not running, `consecutiveFailures > 0`                | `withPicked(false).withFailing(true)`         | `counts.failing`|
| Running   | picked                                                | `withPicked(true)`                            | `counts.running`|
| Overdue   | not running, execution time in the past               | `withPicked(false)` + `before(now)` + lower bound (see *Backend*) | none (see *db-scheduler version*) |
| Scheduled | not running, not failing                              | `withPicked(false).withFailing(false)`        | `counts.scheduled` |

- Overdue **overlaps** Failing (a failed instance whose retry is due) and Scheduled. That is
  intended: it answers "what should have run by now", not a partition.
- **Default tab** when `status` is absent: **Failing** if the task has failing instances,
  otherwise **All**. The default is not written into the URL. The list waits for the summary
  before its first fetch, so it does not load All and then jump to Failing.
- Switching tab keeps `from`; it resets the loaded pages.

### List

- **Layout:** content capped at ~78rem so the columns stay together on wide screens. Rows are cards tinted by status with the Overview's row colours (failing red, running blue). One toolbar row holds go-to-instance, the Start-from control and, on the right, Rerun all failed.
- **Time sections:** rows sit under headings computed from the loaded rows: one Running now and one Overdue section first (running rows usually have a past execution time, so in time order they sit among the overdue ones), then Next hour, Later today, Tomorrow and one per later day.
- **Columns:** instance id · status (All tab only; elsewhere it repeats the tab) · next run (labelled **Retry** on the Failing tab) · last run · consecutive failures.
  - *Instance id:* full, monospace, wraps rather than being cut. Below it is a **one-line muted
    task-data preview**, only when task data is shown (`db-scheduler-ui.task-data`) and non-null.
    The server truncates it (≈100 chars of compact JSON); the full payload is in the expanded details.
    A preview that parses as a flat JSON object is shown as key/value chips (up to four);
    anything else, including a preview cut short, as one line of text.
  - *Status:* Failed / Running / Scheduled (per `../instance-panel/spec.md` §Status derivation).
  - *Next run / last run:* same rules and colours as the Overview columns: relative time,
    absolute time on hover, `running now`, overdue as `due 3m ago` in the warning colour,
    `never run`.
  - *Failures:* `consecutiveFailures`, emphasised red when > 0, `—` when 0.
- **Order:** `execution_time` ascending, then `task_instance`, which is the database's keyset
  order. No sort controls.
- **Paging:** keyset, **50 per page**, a **Load 50 more** button beside `Showing X of Y`; when `next` is null the button goes and the line reads `All N shown`.
- **Start from:** a compact `From: earliest ▾` button opening forward presets `now`, `+1 hour`, `+6 hours`, `+1 day`, `+1 week` (most rows are in the future), backward ones `earliest`, `−1 day`, `−1 hour`, and the browser's own date-time field, to the second (its field order follows the browser's locale; a page cannot change it). The button and the empty state show the start time as `yyyy.mm.dd hh:mm:ss` (`earliest` clears
  `from`). It sets `from` and restarts the list at that execution time. It combines with every
  tab.
- **Go to instance:** an exact-id box, placeholder "Exact instance id" (db-scheduler can look
  an instance up but not search them). On submit it sets `?instance=<id>`; an id outside the
  loaded rows opens in a card above the list, and an unknown id says no scheduled instance has
  it and that ids must match exactly. No prefix or contains search.
- **Expanding:** clicking a row, or its **Details ⌄** button, expands the instance's details
  under it in the same card (animated open and closed); clicking again collapses it. One row is
  open at a time. The open row is scrolled into view.
- **History** (`?instance=`): opening a row pushes a history entry, so Back collapses it;
  switching to another row replaces that entry; collapsing pops it rather than pushing a new
  one, so Back afterwards leaves the page. A row open on arrival (reload, shared link)
  collapses by replacing the entry.
- **Empty states:** an empty tab gets one muted line ("No failing instances"); with a start time set it says so and offers a way back ("No instances from 25 Nov, 21:19 on · show from the earliest"). If the task no
  longer has executions and is not registered, show "No scheduled instances for this task" with
  a link back to the Overview.

### History link

With `history=true`, a **`View history for this task →`** link opens the History page filtered
by task name, the same way the details' `View full history →` does. A task-level history
section is **not** part of this spec; decide that once the page is in use.

## Actions

### Rerun all failed (task level)

- Button **`Rerun all N failed…`**, where N is `counts.failing`. The label names the scope.
  Hidden when N = 0 and when `read-only=true`.
- The confirm dialog says it targets **every failing instance of the task, not only the loaded
  rows**, and that a rerun **resets each instance's failure count and last-failure time**.
- **No eternal loop.** The client sends the moment the dialog was confirmed as `cutoff`. The
  server reruns only instances that are failing, not picked, **and whose `last_failure` is
  before the cutoff**. An instance that fails again after the rerun has a later
  `last_failure`, so this action never picks it up a second time. (Not `execution_time`: a
  failure handler usually moves a failed execution to a *later* retry, so filtering on
  execution time would skip most failing instances.)
- It applies to the whole task regardless of the active tab or `from`.
- The response returns how many instances were rescheduled (some may have been picked or
  finished meanwhile). Toast it, then refetch the summary and the list.

### Per instance

Unchanged instance actions (rerun / delete), now addressed by id. **Rerun** in the details gets the
same reset-warning copy. This resolves `../future_features.md` *Rerun silently clears the
failure history*; remove that entry.

## Backend

### `GET /db-scheduler-api/tasks/executions`

New controller method next to `/tasks/instance`, gated by the overview flag.

```
?taskName=<required>&status=all|failing|running|overdue|scheduled&after=<cursor>&from=<instant>&limit=50

ExecutionPage {
  items: ExecutionRow[]
  next:  string | null     // ExecutionTimeAndId.toEncodedString() of the last item; null = no more
}
ExecutionRow {
  taskInstance, status (FAILED|RUNNING|SCHEDULED), executionTime, picked, pickedBy,
  lastSuccess, lastFailure, consecutiveFailures,
  taskDataPreview: string | null      // truncated compact JSON; null when hidden or absent
}
```

- `getScheduledExecutionsForTask(taskName, Object.class, filter)` with the filter **always
  built explicitly** (the single-argument overload hides picked rows) and `limit(limit + 1)` to
  detect a further page.
- **Cursor:** `after` is db-scheduler's `ExecutionTimeAndId` encoded string, passed through
  as-is. `from` without a cursor becomes `after(new ExecutionTimeAndId(from, ""))`. A cursor wins
  over `from`.
- **Overdue ordering trap:** in db-scheduler `before(...)` used alone switches the query to
  descending order. Overdue therefore always sends a lower bound as well:
  `after(cursor ?? from ?? <epoch sentinel>)` together with `before(now, "")`, which runs as an
  ascending range.
- `limit` is clamped server-side (≤ 200).
- `status` derivation and the `showTaskData` rule reuse `InstanceService`'s logic. Share it,
  don't copy it.

### `POST /db-scheduler-api/tasks/rerunFailed`

On `TaskAdminController`, so it is absent under `read-only=true`.

```
?taskName=<required>&cutoff=<instant, required>   →   { rescheduled: int }
```

Pages `withPicked(false).withFailing(true)` in ascending batches (e.g. 500), keeping rows whose
`last_failure` is before the cutoff. Each goes through `tryReschedule(instance, rerunAt)`, with
`rerunAt` taken once per call. The client's cutoff is clamped to `rerunAt`, so a client clock running ahead of the server cannot let one call rerun an execution that failed again meanwhile. That skips rows picked or gone meanwhile; it returns false rather
than throwing. Rows already at `rerunAt` were moved there by this call and are skipped when the walk
reaches them again. The legacy `/tasks/rerunGroup` stays untouched for the Scheduled page.

### `/tasks/instance`

Unchanged contract. The drawer now sends `id` on the task page; the sole-instance path stays for
the Overview.

## Frontend pieces

- `TaskPage` (route component), `useTaskExecutions` (`useInfiniteQuery` keyed on taskName,
  status, from), and `useTaskPageParams` (the query-string round trip, styled like
  `useOverviewFilters`).
- `InstanceDrawer` / `useInstanceDetail` / `useSelectedInstance` take an optional instance id;
  the task page uses the `instance` param, the Overview keeps `task`.
- The shared Overview wording helpers mentioned under *Header*.
- **Auto-refresh**, when on, refetches the summary and the loaded pages on the Overview's
  interval. *(Builder's call how much of the infinite query to refetch; at minimum the first
  page and the counts.)*

## Degradation

- **`read-only=true`:** no Rerun-all-failed button, no instance actions (as today).
- **`history=false`:** no history link; details behave as today.
- **Task data hidden:** no preview line.
- **No task definitions** (SchedulerClient-only): unaffected. The page needs only the
  executions and the summary.
- **Overview flag off:** route and endpoints absent (404), covered by a targeted test like the
  existing ones.

## Tests

- Endpoint: each status against an H2 fixture with mixed states; keyset continuity (no row
  skipped or duplicated across pages, including equal `execution_time`); `from`; the Overdue
  ascending range; `limit` clamp; preview truncation and hiding; picked rows present in All and
  Running.
- `rerunFailed`: only failing, unpicked, last failure before the cutoff, including failures
  waiting for a later retry; an instance rerun during the call is
  not rerun twice; the read-only context has no endpoint.
- Smoke test in `example-app` for both endpoints; flag-off test.
- Example data: `example-app` needs a task with enough instances (hundreds, a handful failing,
  some overdue) to exercise paging and every tab.

## Delivery

- **One PR** against `main`, with a commit per step:
  1. db-scheduler 17.x bump + starter migration (*db-scheduler version*).
  2. Endpoint + tests.
  3. Page, tabs, list; the Overview links multi-instance rows here.
  4. Drawer by id, `?instance=`, go-to-instance, `Open task page →`.
  5. Rerun all failed + reset-warning copy.
  6. Index recommendation in README and example-app schema.
- **Screenshots after it is built**, not mockups before: once the page runs, capture it with the
  `explore-ui` skill (Vite dev mode) into `screenshots/` here: Failing default tab, All with
  Load more, Overdue, drawer open, read-only, empty tab. Then iterate on those.

## Out of scope

- Retiring `/scheduled/:taskName`, `/tasks/details` or `/tasks/rerunGroup`.
- A task-level history section, run sparkline or grouping failures by exception.
- Prefix/contains instance search, sort controls, multi-select and bulk delete, "Run all".
- The paused/deactivated state (db-scheduler master, unreleased).
- Reschedule (no backend; `../future_features.md`).
