# Overview header — summary strip + filter chips (implementation spec)

Adds a **global summary strip** and **quick-filter chips** to the top of the Overview
page. Builds directly on the task table from `../overview-tasks-table/spec.md` (MVP 1) —
read that first; this spec reuses its `/tasks/overview` endpoint and `OverviewTask` shape.

Promoted out of `../future_features.md` (Header++ + Quick-filter chips). The **task-name
search box stays deferred** there.

## Page structure

The header sits between the `All tasks` H1 and the column headers / sections:

```
All tasks                                                       ← H1
┌─────────┬──────────────────┬───────────┬──────────────┬───────────────────────┐
│ N tasks │ N failing        │ N running │ N scheduled  │ last hour  N ok · M fail │  ← summary strip
│         │ · M instances    │           │              │ (only if history=true)   │
└─────────┴──────────────────┴───────────┴──────────────┴───────────────────────┘
[ Has failures ] [ Running now ] [ Recurring only ]   Showing X of Y · Clear       ← filter chips
TASK                  NEXT RUN     LAST RUN                     ← column headers
RECURRING · 5
● <name>  …
```

- Strip + chips are **navigation/view-only** ⇒ inherently safe under `read-only=true`
  (no Run/Rerun/Delete), consistent with the rest of the Overview.
- The **strip always shows totals** over the full task set (it is the "is anything wrong"
  glance). Chips filter only the **table**, never the strip numbers.

## Summary strip

Five stats, left → right. Each pairs a number with a **text label** (color is never the
only signal — a11y, matching the table spec).

| Stat           | Value                                                                         | Level           | Color                 |
|----------------|-------------------------------------------------------------------------------|-----------------|-----------------------|
| **Tasks**      | count of distinct task names (incl. dormant)                                  | task            | neutral               |
| **Failing**    | tasks with ≥1 failing instance; sub `· M instances` = total failing instances | task + instance | red (muted when 0)    |
| **Running**    | total running (picked) instances                                              | instance        | blue (muted when 0)   |
| **Scheduled**  | total scheduled instances                                                     | instance        | neutral               |
| **Throughput** | `last hour  N ok · M failed`                                                  | instance        | ok green / failed red |

Why the mixed levels: an operator asks *"how many of my tasks are broken?"* (task-level
for **Failing**) but *"how much work is queued / churning?"* (instance-level for
**Running** / **Scheduled**). Failing therefore leads with a task count and appends the
instance count as a sub; running/scheduled are instance totals.

- **Zero is calm, not alarming.** `0 failing` / `0 running` render in the muted color, not
  red/blue. Reserve the alert color for `> 0`.
- **Throughput is history-gated.** Render the whole stat **only when `history=true`**
  (`/db-scheduler-api/config`); **omit it entirely** otherwise — no disabled/`0·0`
  placeholder.

## Filter chips

Three **independent toggle** chips, **AND-combined**. They filter the already-loaded list
**client-side** (the Overview has no pagination — all rows are present), so chips combine
freely and need no server round-trip.

| Chip              | Keeps tasks where        | Source field            |
|-------------------|--------------------------|-------------------------|
| **Has failures**  | `counts.failing > 0`     | `OverviewTask.counts`   |
| **Running now**   | `counts.running > 0`     | `OverviewTask.counts`   |
| **Recurring only**| `recurring === true`     | `OverviewTask.recurring`|

- **Toggle semantics:** click to activate (filled), click again to clear (outline). Active
  chips combine with AND.
- **Click-to-filter from the strip:** clicking the **Failing** stat toggles *Has failures*;
  clicking **Running** toggles *Running now*. The strip numbers stay as totals regardless.
- **Empty sections under a filter:** keep the table's RECURRING / ONE-TIME-CUSTOM section
  headers; **hide a section** when it has zero matches (e.g. *Recurring only* empties the
  One-time / Custom section — expected). Section `· N` counts update to the **matched**
  count.
- **Active-filter affordance:** when ≥1 chip is active, show a muted `Showing X of Y
  tasks · Clear` line; **Clear** resets all chips.
- **Strictly alphabetical order is preserved** within each section after filtering — chips
  only remove rows, never reorder (matches the table spec's stable-position rule).

### Why client-side (not the legacy `filter=` param)

`QueryUtils` server-side filtering (`ALL | FAILED | RUNNING | SCHEDULED | SUCCEEDED`) drives
the **Scheduled** list. The Overview list is fully materialized and bounded by
task-definition count, so filtering in the browser is simpler, lets the three chips combine,
and keeps the `/tasks/overview` endpoint param-free. **Do not** thread these chips through
the server-side filter.

### URL sync (recommended, optional)

Encode active chips in the query string (e.g. `?failing&running&recurring`) so refresh,
back/forward, and shared links restore filter state. Not required for the first cut.

## Backend

**Strip stats need no new backend** — derive them client-side by reducing the
`/tasks/overview` array (one `OverviewTask` per task name):

```
tasks              = list.length
failingTasks       = count(t => t.counts.failing > 0)
failingInstances   = sum(t => t.counts.failing)
runningInstances   = sum(t => t.counts.running)
scheduledInstances = sum(t => t.counts.scheduled)
```

- **Recurring chip** uses the existing `OverviewTask.recurring` flag (added in MVP 1).
- **Throughput** is the *only* server-side addition, and only when `history=true`. It needs
  the log table, not the execution rows. Recommended: extend the overview payload with an
  **optional** block computed via `LogLogic` over a trailing 60-minute window, `null` when
  history is off:

  ```
  OverviewResponse {
    tasks: OverviewTask[]
    throughput: { ok: int, failed: int } | null   // null ⇒ history disabled
  }
  ```

  Folding it into the existing payload avoids a second round-trip (the page already needs
  `/config` for the history + read-only flags). A separate
  `GET /db-scheduler-api/tasks/overview/throughput?window=1h` is an acceptable alternative.

## Graceful degradation

Consistent with the table spec's SchedulerClient-only fallback:

- **`recurring` is null** (no task definitions / degraded mode) ⇒ **hide the *Recurring
  only* chip** (it can't be evaluated). Strip + other two chips still work.
- **`history=false`** ⇒ hide the throughput stat.
- **Zero tasks** (fresh scheduler) ⇒ strip shows `0 tasks`, other stats muted; **hide the
  chips** (nothing to filter). Page-level empty state is the table spec's open question, not
  re-specified here.

## Visual / tokens

Reuse the Overview tokens from the table mockup (`--ov-red`, `--ov-blue`, `--ov-green`,
`--ov-grey`, `--ov-muted`). The strip cells mirror the per-task header's `.summary-cell`
styling (`.bad` = red, `.run` = blue) but at page scope. Chips: outline when inactive,
filled/tinted when active, always text-labeled.

> Mockups: `screenshots/01-overview-header.png` (strip + chips, no filter active) and
> `screenshots/02-overview-filtered.png` (*Has failures* chip active, sections filtered).
> The base table mockup (`../overview-tasks-table/screenshots/01-overview.png`) has no strip
> or chips — these sit above the task table.

## Relevant existing code

- Overview endpoint + `OverviewTask` model — `../overview-tasks-table/spec.md` §Backend.
- Config flags (history, read-only): `…/ui/controller/ConfigController.java` →
  `GET /db-scheduler-api/config`.
- Throughput source: `…/ui/service/LogLogic.java` (log table).
- Legacy server-side filter (Scheduled list only — **not** reused here):
  `…/ui/util/QueryUtils.java`.
- Frontend Overview page: `…/src/pages/FrontPage.tsx` — render strip + chips above the table.
- Relative-time util (throughput/last-hour copy): `…/src/utils/dateFormatText.ts`.
- New components: `SummaryStrip` (a.k.a. `HealthStrip`) and `FilterChips`.

## Out of scope (deferred — see `../future_features.md`)

- **Task-name search box** — stays in `future_features.md`.
- Sparkline / historical trend on the throughput stat.
- "Problems first" / severity sort toggle (alphabetical-only for now).
- Auto-refresh with live diff highlighting.
- Linking the throughput stat into a filtered History view (optional, not required).
