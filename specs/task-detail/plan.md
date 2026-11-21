# Task detail Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A `/tasks/:taskName` page listing one task's executions under status tabs, keyset-paged
through the database, with the existing instance drawer for the selected row and a safe
"Rerun all failed".

**Architecture:** A new `ExecutionPager` service turns `(task, status, cursor/from, limit)` into
one seeked `SchedulerClient` query using db-scheduler's `withPicked` / `withFailing` / `after` /
`before` / `limit`. A `FailedRerunner` walks the failing set in batches below a cutoff. The
frontend adds a route, a page built from extracted Overview wording helpers, and teaches the
drawer to open by instance id.

**Tech Stack:** Java 17, Spring Boot 3 + 4 starters, db-scheduler 17.x, H2 (tests), React 18 +
Chakra UI + React Query v4 + react-router v6.

**Spec:** `specs/task-detail/spec.md`. Read it first; this plan argues from it.

## Global Constraints

- db-scheduler: the first release containing `ScheduledExecutionsFilter.withFailing` (≥ 17.2.0;
  take the exact version from Maven Central). Set it for both `db-scheduler.version` and
  `db-scheduler-boot4.version`.
- Everything new is gated by `db-scheduler-ui.overview=true`. `rerunFailed` is also absent when
  `db-scheduler-ui.read-only=true`.
- No request may load a task's full execution set; nothing new goes through `TaskLogic` or `Caching`.
- Page size default **50**, server clamp **200**. Rerun batch size **500**. Task-data preview
  **100 chars** max.
- Order is always `execution_time asc, task_instance asc`.
- Instance ids go in the query string (`instance=`), never in the path.
- Mirror every starter change in **both** `db-scheduler-ui-starter` and
  `db-scheduler-ui-spring-boot-4-starter`.
- License headers on new `.java`/`.ts`/`.tsx`; `./mvnw spotless:apply` and `license:format`
  before finishing. Comments follow `CLAUDE.md` "Code comments".
- **This VM:** cap every JVM (`MAVEN_OPTS="-Xmx512m"`, app JVMs `-Xmx384m`), run under `nice`,
  sequential (no `-T`), and check `free -m` first. Iterate with `-Pfast -Dtest=...`.

## Review Focus

1. **Equal `execution_time` across a page boundary.** Many instances scheduled at the same
   instant must page without skips or duplicates: the cursor carries `task_instance`. Test:
   Task 2, `paging_equalExecutionTimes_noSkipOrDuplicate`.
2. **A running instance that is also failing.** It appears in All and Running, not in Failing or
   Scheduled, and its row says Running. Test: Task 2, `pickedAndFailing_onlyInAllAndRunning`.
3. **Instance ids with `/`, `:`, spaces, `%`.** These must round-trip through the cursor,
   `?instance=` and the drawer fetch. Test: Task 2, `cursor_roundTripsAwkwardIds`. In Task 5,
   manual check in the screenshot pass.
4. **Rerun racing the scheduler.** Rows picked or completed between read and reschedule are
   skipped, not errors, and never rerun twice. Test: Task 3,
   `rerun_skipsRowsPickedMeanwhile`, `rerun_twice_secondRunFindsNothing`.
5. **Task name with `/` or spaces in the route.** `/tasks/:taskName` must decode it, and the
   breadcrumb and API calls must re-encode it. Manual check in Task 6; the example data includes
   no such name, so add one temporarily when screenshotting.

---

### Task 1: db-scheduler 17.x bump and starter migration

**Files:**
- Modify: `pom.xml` (both version properties), `README.md` (minimum version line ~32)
- Create: `db-scheduler-ui-starter/src/main/java/no/bekk/dbscheduler/uistarter/autoconfigure/SchedulerSettings.java`
  and the same path under `db-scheduler-ui-spring-boot-4-starter`
- Modify: both starters' `autoconfigure/UiApiAutoConfiguration.java` (`instanceLogRepository`,
  `logLogic`) and `log/DbSchedulerUiLogAutoConfiguration.java` (`logRepository`)
- Test: `…/uistarter/autoconfigure/SchedulerSettingsTest.java` in both starters

**Interfaces:**
- Produces: `record SchedulerSettings(Optional<DataSource> dataSource, Optional<Serializer> serializer)`
  with `static SchedulerSettings resolve(@Nullable DbSchedulerOverrides overrides, @Nullable DbSchedulerCustomizer customizer)`.
  Precedence: overrides wins whole, else customizer, else both empty. Logs nothing; db-scheduler's
  own starter already warns about the deprecated customizer.

- [ ] **Step 1:** Bump both version properties and the README minimum. Build with
  `MAVEN_OPTS=-Xmx512m nice ./mvnw -q -Pfast compile`. Expected: it compiles, or it fails only where
  `DbSchedulerCustomizer` is used (deprecation is fine).
- [ ] **Step 2: Failing test** `SchedulerSettingsTest`:
  `noBeans_bothEmpty`, `overridesOnly_usesOverrides`, `customizerOnly_usesCustomizer`,
  `both_overridesWinWhole` (overrides with a data source and no serializer, customizer with a
  serializer: result has the data source and an empty serializer).
- [ ] **Step 3:** Implement `SchedulerSettings.resolve`. Replace every `DbSchedulerCustomizer`
  parameter with `ObjectProvider<DbSchedulerOverrides>` + `ObjectProvider<DbSchedulerCustomizer>`
  resolved through it. In `logRepository` keep the lazy lookup: resolve inside the serializer
  supplier, not at bean creation (`DbSchedulerUiLogAutoConfigurationCycleTest` guards the cycle).
- [ ] **Step 4:** `nice ./mvnw -q -Pfast test` passes. Then run the example-app `SmokeTest` once.
  The example app defines neither bean, so this is the no-bean startup case:
  `nice ./mvnw -q test -pl example-app -Dexec.skip=true -Dtest=SmokeTest`. Expected: PASS.
- [ ] **Step 5: Commit** `build: require db-scheduler <version>; resolve scheduler overrides optionally`

### Task 2: `GET /tasks/executions`

**Files:**
- Create in `db-scheduler-ui/src/main/java/no/bekk/dbscheduler/ui/`:
  `model/ExecutionStatusFilter.java` (enum `ALL, FAILING, RUNNING, OVERDUE, SCHEDULED`),
  `model/ExecutionRow.java`, `model/ExecutionPage.java`, `service/ExecutionPager.java`,
  `util/TaskDataPreview.java`
- Modify: `model/InstanceStatus.java` (add `static InstanceStatus of(ScheduledExecution<?>)`,
  moved from `InstanceService.status`, which then calls it), `controller/InstanceController.java`
  (new mapping), both starters' `UiApiAutoConfiguration` (an `ExecutionPager` bean, overview-gated,
  `@ConditionalOnMissingBean`, injected into `InstanceController`)
- Create test support: `db-scheduler-ui/src/test/java/no/bekk/dbscheduler/ui/testsupport/ScheduledTasksTable.java`
  (`static void createSchema(DataSource)`: db-scheduler's postgresql DDL, H2-compatible, plus the
  recommended index; `static void markFailing(DataSource, String task, String id, int failures)`;
  `static void markPicked(DataSource, String task, String id)`)
- Test: `db-scheduler-ui/src/test/java/no/bekk/dbscheduler/ui/service/ExecutionPagerTest.java`,
  `util/TaskDataPreviewTest.java`; extend `example-app/src/test/.../SmokeTest.java`

**Interfaces:**
- Consumes: `InstanceStatus.of(...)`.
- Produces:
  - `record ExecutionRow(String taskInstance, InstanceStatus status, Instant executionTime, boolean picked, String pickedBy, Instant lastSuccess, Instant lastFailure, int consecutiveFailures, String taskDataPreview)`
  - `record ExecutionPage(List<ExecutionRow> items, String next)`. `next` is
    `ExecutionTimeAndId.toEncodedString()` of the last item, null when there is no further page.
  - `ExecutionPager(SchedulerClient client, boolean showTaskData, Clock clock)` with
    `ExecutionPage page(String taskName, ExecutionStatusFilter status, @Nullable String after, @Nullable Instant from, @Nullable Integer limit)`
  - `GET /db-scheduler-api/tasks/executions?taskName&status&after&from&limit`. `status` defaults
    to `ALL` and is case-insensitive; an unknown value or a bad cursor → 400.
  - `TaskDataPreview.of(Object data, boolean show) -> String` (null when hidden or null;
    compact JSON via a static Jackson `ObjectMapper` with `findAndRegisterModules()`, falling back
    to `String.valueOf`; cut to 100 chars with a trailing `…` when longer).

- [ ] **Step 1: Failing tests** in `ExecutionPagerTest`. Use a real `SchedulerClient`
  (`SchedulerClient.Builder.create(dataSource, tasks…).build()`) over
  `TestDatabase` + `ScheduledTasksTable`, and a fixed `Clock`. Seed via `scheduleIfNotExists`,
  then `markFailing` / `markPicked`.
  - `all_returnsEveryExecutionInTimeOrder`
  - `failing_onlyUnpickedWithFailures`, `scheduled_onlyUnpickedWithoutFailures`, `running_onlyPicked`
  - `pickedAndFailing_onlyInAllAndRunning` (status `RUNNING`)
  - `overdue_unpickedInThePast_ascending`: rows at now−3h, now−1h, now+1h → `[now−3h, now−1h]`
  - `overdue_paging_staysAscending`: limit 1, follow `next` twice
  - `paging_equalExecutionTimes_noSkipOrDuplicate`: 7 rows at the same instant, limit 3 → 3+3+1,
    all distinct, last `next` null
  - `from_startsAtThatTime`, `cursorWinsOverFrom`
  - `limit_defaultsTo50_clampsTo200`
  - `cursor_roundTripsAwkwardIds`: ids `a/b`, `c:d`, `e f`, `g%h` across pages
  - `otherTasksExecutions_neverIncluded`
  - `TaskDataPreviewTest`: `hidden_isNull`, `null_isNull`, `short_isCompactJson`,
    `long_isCutTo100WithEllipsis`
- [ ] **Step 2:** Run `nice ./mvnw -q -Pfast test -pl db-scheduler-ui -Dtest='ExecutionPagerTest,TaskDataPreviewTest'`.
  Expected: compile failure / FAIL.
- [ ] **Step 3: Implement `ExecutionPager.page`.** Build `ScheduledExecutionsFilter.all()`:
  - FAILING: `withPicked(false).withFailing(true)`
  - SCHEDULED: `withPicked(false).withFailing(false)`
  - RUNNING: `withPicked(true)`
  - OVERDUE: `withPicked(false)` plus `before(new ExecutionTimeAndId(now, ""))` and **always** an
    `after(...)`. Use the cursor, else `from`, else `new ExecutionTimeAndId(Instant.EPOCH, "")`.
    A `before` alone sorts descending.
  - All other statuses: `after(cursor)` or `after(new ExecutionTimeAndId(from, ""))` when given.

  Then `limit(limit + 1)`, `getScheduledExecutionsForTask(taskName, Object.class, filter)`. Trim
  to `limit`, and set `next` only when the extra row existed. Decode the cursor with
  `ExecutionTimeAndId.fromEncodedString`; map a decode failure to an `IllegalArgumentException`
  that the controller turns into 400.
- [ ] **Step 4:** Add the controller mapping and the bean in both starters. Extend
  `SmokeTest`: `testGetExecutionsReturnsPage` (200, JSON has `items`) and add
  `context.containsBean("executionPager")` to `testContextLoads`.
- [ ] **Step 5:** The tests from Step 2 pass, then
  `nice ./mvnw -q test -pl example-app -Dexec.skip=true -Dtest=SmokeTest` passes.
- [ ] **Step 6: Commit** `feat: keyset-paged task executions endpoint`

### Task 3: `POST /tasks/rerunFailed`

**Files:**
- Create: `db-scheduler-ui/src/main/java/no/bekk/dbscheduler/ui/service/FailedRerunner.java`,
  `controller/ExecutionAdminController.java`
- Modify: both starters' `UiApiAutoConfiguration`: beans for both classes, gated by
  `@ConditionalOnExpression("${db-scheduler-ui.overview:false} and !${db-scheduler-ui.read-only:false}")`
  (a separate controller, so that `TaskAdminController` keeps its single read-only gate)
- Test: `db-scheduler-ui/src/test/.../service/FailedRerunnerTest.java`; extend `SmokeTest`,
  `SmokeReadOnlyTest`

**Interfaces:**
- Produces: `FailedRerunner(SchedulerClient client, Clock clock, int batchSize)` with
  `int rerunFailed(String taskName, Instant cutoff)`. It returns the number rescheduled. Endpoint
  `POST /db-scheduler-api/tasks/rerunFailed?taskName&cutoff` → `{"rescheduled": n}`, where
  `record RerunFailedResponse(int rescheduled)`.

- [ ] **Step 1: Failing tests** (same H2 setup as Task 2, batch size 3):
  - `rerun_onlyFailingUnpickedBeforeCutoff`: failing@now−1h ✔, failing@now+1h ✘ (after the
    cutoff), scheduled@now−1h ✘, picked+failing ✘ → returns 1, and that row's `execution_time`
    is now
  - `rerun_walksAllBatches`: 8 failing rows, batch 3 → 8
  - `rerun_twice_secondRunFindsNothing`: same cutoff twice → n, then 0
  - `rerun_skipsRowsPickedMeanwhile`: a row picked after being read is not counted (simulate with
    a `SchedulerClient` wrapper that marks it picked just before `reschedule`)
- [ ] **Step 2:** Run it. Expected: FAIL.
- [ ] **Step 3:** Implement. Page `withPicked(false).withFailing(true)` with
  `after(last ?? EPOCH sentinel)` + `before(new ExecutionTimeAndId(cutoff, ""))` +
  `limit(batchSize)`. For each row, `reschedule(taskInstanceId, clock.instant())`; count `true`
  results. Stop on a short batch.
- [ ] **Step 4:** Add the controller and beans. `SmokeTest`: `testRerunFailedReturnsCount` (200).
  `SmokeReadOnlyTest`: assert no `executionAdminController` bean. New
  `example-app/src/test/.../SmokeOverviewOffTest.java` (`@TestPropertySource(properties = "db-scheduler-ui.overview=false")`,
  same annotations as `SmokeTest`): no `executionPager` or `executionAdminController` bean, and
  `GET /db-scheduler-api/tasks/executions?taskName=x` → 404.
- [ ] **Step 5:** Tests pass, including both smoke classes.
- [ ] **Step 6: Commit** `feat: rerun a task's failed executions below a cutoff`

### Task 4: Frontend data layer and shared wording

**Files:**
- Create: `db-scheduler-ui-frontend/src/models/ExecutionPage.ts`,
  `src/services/getTaskExecutions.ts`, `src/services/rerunFailed.ts`,
  `src/hooks/useTaskPageParams.ts`, `src/hooks/useTaskExecutions.ts`,
  `src/utils/overviewWording.tsx`
- Modify: `src/pages/OverviewPage.tsx`: move `statusText`, `statusColors`, `dotColor`, `overdue`,
  `subLine`, `statusDuration`, `nextRunText`, `lastRunText`, `parseDate`, `absoluteTitle` and
  `countPart` into `overviewWording.tsx` unchanged and import them back

**Interfaces:**
- Produces:
  - `type ExecutionStatusFilter = 'all' | 'failing' | 'running' | 'overdue' | 'scheduled'`
  - `interface ExecutionRow { taskInstance; status: InstanceStatus; executionTime: string; picked; pickedBy: string | null; lastSuccess: string | null; lastFailure: string | null; consecutiveFailures: number; taskDataPreview: string | null }`,
    `interface ExecutionPage { items: ExecutionRow[]; next: string | null }`
  - `getTaskExecutions(taskName, status, after?: string, from?: string): Promise<ExecutionPage>`,
    with `TASK_EXECUTIONS_QUERY_KEY = 'tasks/executions'`. Same 401 handling as `getInstanceDetail`.
  - `rerunFailed(taskName, cutoff: Date): Promise<{ rescheduled: number }>`
  - `useTaskPageParams(): { status: ExecutionStatusFilter | null; setStatus; from: string | null; setFrom; instance: string | null; openInstance; closeInstance }`.
    `status`/`from` use replace; `instance` uses push (back closes the drawer). Unknown `status`
    values read as null.
  - `useTaskExecutions(taskName, status, from, { enabled, refetchInterval })`: `useInfiniteQuery`
    keyed `[TASK_EXECUTIONS_QUERY_KEY, taskName, status, from]`, with
    `getNextPageParam: (p) => p.next ?? undefined`

- [ ] **Step 1:** Extract the wording helpers. `pnpm run lint && pnpm run build` passes, and the
  Overview renders identically (spot check in Task 7's screenshots).
- [ ] **Step 2:** Add the models, services and hooks. `pnpm run lint && pnpm run build` passes.
- [ ] **Step 3: Commit** `refactor: share overview wording; add task execution data layer`

### Task 5: Task page

**Files:**
- Create: `src/pages/TaskPage.tsx`, `src/components/task/StatusTabs.tsx`,
  `src/components/task/ExecutionTable.tsx`, `src/components/task/StartFromControl.tsx`
- Modify: `src/pages/FrontPage.tsx` (route `/tasks/:taskName` inside the `showOverview` block),
  `src/pages/OverviewPage.tsx` (`drillDownTarget` → `/tasks/${encodeURIComponent(name)}`,
  passing `state: { from: location }` for the breadcrumb)

**Interfaces:**
- Consumes: Task 4's hooks, services and wording helpers. The header's `OverviewTask` comes from
  `useQuery([OVERVIEW_TASKS_QUERY_KEY], getOverviewTasks)`, picked by name.

Behaviour, all from spec §Page structure:
- [ ] **Step 1:** `TaskPage`: breadcrumb (`location.state.from` or `/overview`), header from the
  wording helpers, the auto-refresh switch (`useAutoRefresh`, same 2000 ms interval as Overview).
  Default tab: if `status` is null, use `failing` when `counts.failing > 0`, else `all`. Don't
  fetch executions until the summary has loaded. Empty states: "No <tab> instances", and
  "No scheduled instances for this task" plus a link to the Overview when the task is absent from
  the summary.
- [ ] **Step 2:** `StatusTabs`: Chakra `Tabs`; badges `instanceCount`, `counts.failing`,
  `counts.running`, none for Overdue, `counts.scheduled`.
- [ ] **Step 3:** `StartFromControl`: a `datetime-local` input plus `now`, `−1h`, `−1d` and
  `earliest` buttons. `earliest` clears `from`.
- [ ] **Step 4:** `ExecutionTable`: columns per spec. The id is monospace and wraps, with the muted
  one-line preview below. Next/last run use the Overview rules. Failures in red when > 0,
  `—` otherwise. Clicking a row calls `openInstance(id)`; the selected row is highlighted.
  `Load more` / `Nothing more to load`.
- [ ] **Step 5:** `pnpm run lint && pnpm run build` passes. Smoke-run in Vite dev mode against the
  example app (see the `explore-ui` skill, mode B): a multi-instance Overview row lands on the
  page, tabs switch, and Load more appends.
- [ ] **Step 6: Commit** `feat: task detail page with status tabs and keyset list`

### Task 6: Drawer by id, navigation links, rerun all failed

**Files:**
- Modify: `src/services/getInstanceDetail.ts` (optional `id` param),
  `src/hooks/useInstanceDetail.ts` (key includes `id`),
  `src/components/overview/InstanceDrawer.tsx` (props
  `{ taskName: string | null; instanceId?: string | null; onClose; onChanged; showTaskPageLink?: boolean }`;
  `Open task page →` link when `showTaskPageLink`; rerun confirm copy adds "This resets its
  failure count and last-failure time.")
- Create: `src/components/task/GoToInstance.tsx`, `src/components/task/RerunFailedButton.tsx`
- Modify: `TaskPage.tsx` (drawer with `instanceId`, go-to box, rerun button, history link)
- Modify: `specs/future_features.md`: delete "Rerun silently clears the failure history"

- [ ] **Step 1:** Drawer by id; the Overview passes `showTaskPageLink`; the task page passes
  `instanceId` from `useTaskPageParams`. An unknown id shows the existing "gone" state.
- [ ] **Step 2:** `GoToInstance`: an input + submit that calls `openInstance(trimmed)`.
- [ ] **Step 3:** `RerunFailedButton`: `Rerun all N failed…`, hidden when N=0 or read-only
  (`useConfig`'s read-only flag, as the drawer does). Confirm copy: "Rerun all N failing
  instances of <task>, not only the ones loaded. This resets their failure counts and
  last-failure times." Send `cutoff` = the moment of confirming. Toast
  `Rescheduled <n> instances`, then refetch the summary and the list.
- [ ] **Step 4:** `View history for this task →` (history on): `navigate('/history/all', { state: { taskName } })`.
- [ ] **Step 5:** `pnpm run lint && pnpm run build` passes. In dev mode, rerun from the Failing tab
  moves rows out of it. Back closes the drawer. A task name with a space opens and links
  correctly (Review Focus 5).
- [ ] **Step 6: Commit** `feat: open instances by id, go-to-instance, rerun all failed`

### Task 7: Example data, index recommendation, full verification, screenshots

**Files:**
- Create: `example-app-shared/src/main/java/com/github/bekk/exampleapp/tasks/InvoiceEmailTask.java`
  (one-time task `send-invoice-email`; the data is a small record; it fails when the id ends in `7`
  with `FailureHandler.OnFailureRetryLater(Duration.ofMinutes(30))`, so failures stay visible)
- Modify: `TaskService.runManuallyTriggeredTasks()` schedules 300 instances spread over the next
  6 h (ids `invoice-2026-11-00NNNNN`, some sharing an instant), plus the task bean registration
  wherever the example tasks are wired. Add
  `example-app/src/main/resources/db/migration/V4__add_task_time_index.sql`
  (`create index st_task_time_idx on scheduled_tasks (task_name, execution_time, task_instance);`)
  and the same for the boot4 and webflux example apps if they carry their own migrations.
- Modify: `README.md`: a short "Recommended index" note with that statement.

- [ ] **Step 1:** Add the example data and the migration; `SmokeTest` still passes.
- [ ] **Step 2:** Full verification: `MAVEN_OPTS=-Xmx512m nice ./mvnw spotless:apply license:format -q`,
  then `MAVEN_OPTS=-Xmx512m nice ./mvnw clean install -q`. Expected: BUILD SUCCESS. Delete
  `db-scheduler-application/` afterwards only if git shows it untracked; it is ignored today.
- [ ] **Step 3:** Screenshots via `explore-ui` (Vite dev mode, headless Chromium) into
  `specs/task-detail/screenshots/`: `01-failing-default.png`, `02-all-load-more.png`,
  `03-overdue.png`, `04-drawer-open.png`, `05-read-only.png`, `06-empty-tab.png`.
- [ ] **Step 4: Commit** `feat: example invoice task, task-time index, screenshots`
