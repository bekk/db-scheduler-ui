/*
 * Copyright (C) Bekk
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.bekk.dbscheduler.ui.util.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.github.kagkarlsson.scheduler.TaskSummary;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import no.bekk.dbscheduler.ui.model.OverviewTask;
import no.bekk.dbscheduler.ui.model.WorstStatus;
import org.junit.jupiter.api.Test;

class OverviewMapperTest {

  private static TaskSummary summary(
      String name, int instances, int failing, int running, int scheduled) {
    return new TaskSummary(
        name,
        instances,
        running,
        failing,
        scheduled,
        Instant.parse("2026-06-03T10:00:00Z"),
        Instant.parse("2026-06-03T09:00:00Z"),
        Instant.parse("2026-06-03T09:30:00Z"),
        failing);
  }

  @Test
  void worstStatusFollowsFailingRunningScheduledPrecedence() {
    List<TaskSummary> summaries =
        List.of(
            summary("any-failing", 5, 1, 2, 2),
            summary("running-no-failing", 3, 0, 1, 2),
            summary("only-scheduled", 2, 0, 0, 2));

    List<OverviewTask> rows = OverviewMapper.toOverview(summaries, Set.of(), Set.of(), false);

    assertThat(rows)
        .extracting(OverviewTask::taskName, OverviewTask::worstStatus)
        .containsExactly(
            tuple("any-failing", WorstStatus.FAILING),
            tuple("running-no-failing", WorstStatus.RUNNING),
            tuple("only-scheduled", WorstStatus.SCHEDULED));
  }

  @Test
  void failingInstanceCurrentlyBeingRetriedIsStillFailing() {
    // A picked instance with consecutive_failures > 0 lands in runningCount (not failingCount) per
    // the DB summary, but maxConsecutiveFailures still reflects it — the row must show FAILING.
    TaskSummary pickedFailing =
        new TaskSummary(
            "retrying",
            1,
            1, // runningCount (picked)
            0, // failingCount (un-picked failing) — zero, the failure is mid-retry
            0,
            Instant.parse("2026-06-03T10:00:00Z"),
            null,
            Instant.parse("2026-06-03T09:30:00Z"),
            3); // maxConsecutiveFailures

    List<OverviewTask> rows =
        OverviewMapper.toOverview(List.of(pickedFailing), Set.of(), Set.of(), false);

    assertThat(rows)
        .singleElement()
        .extracting(OverviewTask::worstStatus)
        .isEqualTo(WorstStatus.FAILING);
  }

  @Test
  void overlaysRecurringFlagFromRegisteredDefinitions() {
    List<TaskSummary> summaries =
        List.of(summary("recurring-task", 1, 0, 0, 1), summary("one-time-task", 1, 0, 0, 1));

    List<OverviewTask> rows =
        OverviewMapper.toOverview(
            summaries, Set.of("recurring-task"), Set.of("recurring-task", "one-time-task"), true);

    assertThat(rows)
        .filteredOn(row -> row.taskName().equals("recurring-task"))
        .singleElement()
        .extracting(OverviewTask::recurring)
        .isEqualTo(true);
    assertThat(rows)
        .filteredOn(row -> row.taskName().equals("one-time-task"))
        .singleElement()
        .extracting(OverviewTask::recurring)
        .isEqualTo(false);
  }

  @Test
  void emitsDormantRowsForNonRecurringDefinitionsWithoutExecutions() {
    List<TaskSummary> summaries = List.of(summary("active-one-time", 1, 0, 0, 1));

    List<OverviewTask> rows =
        OverviewMapper.toOverview(
            summaries,
            Set.of("a-recurring"),
            Set.of("active-one-time", "a-recurring", "dormant-import-job"),
            true);

    OverviewTask dormant =
        rows.stream()
            .filter(row -> row.taskName().equals("dormant-import-job"))
            .findFirst()
            .orElseThrow();

    assertThat(dormant.worstStatus()).isEqualTo(WorstStatus.DORMANT);
    assertThat(dormant.recurring()).isFalse();
    assertThat(dormant.instanceCount()).isZero();
    assertThat(dormant.nextExecutionTime()).isNull();
    assertThat(dormant.lastSuccess()).isNull();
    assertThat(dormant.lastFailure()).isNull();
  }

  @Test
  void recurringDefinitionsAreNeverDormant() {
    List<OverviewTask> rows =
        OverviewMapper.toOverview(
            List.of(), Set.of("idle-recurring"), Set.of("idle-recurring"), true);

    // A recurring definition with no executions is an abnormal state, not a dormant row.
    assertThat(rows).isEmpty();
  }

  @Test
  void degradedModeLeavesRecurringNullAndEmitsNoDormantRows() {
    List<TaskSummary> summaries = List.of(summary("some-task", 1, 0, 0, 1));

    List<OverviewTask> rows = OverviewMapper.toOverview(summaries, Set.of(), Set.of(), false);

    assertThat(rows).singleElement().extracting(OverviewTask::recurring).isNull();
  }

  @Test
  void carriesAggregatedTimesAndCountsThrough() {
    TaskSummary summary =
        new TaskSummary(
            "rich",
            7,
            2,
            3,
            2,
            Instant.parse("2026-06-03T12:00:00Z"),
            Instant.parse("2026-06-03T11:00:00Z"),
            Instant.parse("2026-06-03T11:45:00Z"),
            4);

    OverviewTask row =
        OverviewMapper.toOverview(List.of(summary), Set.of(), Set.of(), false).get(0);

    assertThat(row.instanceCount()).isEqualTo(7);
    assertThat(row.counts().failing()).isEqualTo(3);
    assertThat(row.counts().running()).isEqualTo(2);
    assertThat(row.counts().scheduled()).isEqualTo(2);
    assertThat(row.nextExecutionTime()).isEqualTo(Instant.parse("2026-06-03T12:00:00Z"));
    assertThat(row.lastSuccess()).isEqualTo(Instant.parse("2026-06-03T11:00:00Z"));
    assertThat(row.lastFailure()).isEqualTo(Instant.parse("2026-06-03T11:45:00Z"));
    assertThat(row.maxConsecutiveFailures()).isEqualTo(4);
    assertThat(row.worstStatus()).isEqualTo(WorstStatus.FAILING);
  }
}
