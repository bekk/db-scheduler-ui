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
package no.bekk.dbscheduler.ui.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.kagkarlsson.scheduler.TaskSummary;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.helper.ScheduleAndData;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.Schedules;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import no.bekk.dbscheduler.ui.model.OverviewTask;
import no.bekk.dbscheduler.ui.model.OverviewTaskStatus;
import no.bekk.dbscheduler.ui.testsupport.StubSchedulerClient;
import org.junit.jupiter.api.Test;

class OverviewServiceTest {

  private static final Instant NOW = Instant.parse("2026-06-05T10:00:00Z");

  @Test
  void getOverviewTasks_mapsSummariesAndSortsByTaskName() {
    StubSchedulerClient scheduler = new StubSchedulerClient(
      List.of(
        summary(
          "email",
          2,
          1,
          1,
          0,
          NOW.plus(Duration.ofHours(1)),
          NOW.minus(Duration.ofHours(2)),
          NOW.minus(Duration.ofDays(1)),
          2
        ),
        summary("billing", 1, 0, 0, 1, NOW.plus(Duration.ofHours(2)), null, null, 0)
      )
    );

    List<OverviewTask> tasks = new OverviewService(scheduler, List.of()).getOverviewTasks();

    assertThat(tasks).extracting(OverviewTask::getTaskName).containsExactly("billing", "email");
    assertThat(tasks)
      .filteredOn(task -> task.getTaskName().equals("email"))
      .singleElement()
      .satisfies(task -> {
        assertThat(task.getWorstStatus()).isEqualTo(OverviewTaskStatus.FAILING);
        assertThat(task.getInstanceCount()).isEqualTo(2);
        assertThat(task.getCounts().getFailing()).isEqualTo(1);
        assertThat(task.getCounts().getRunning()).isEqualTo(1);
        assertThat(task.getCounts().getScheduled()).isZero();
        assertThat(task.getNextExecutionTime()).isEqualTo(NOW.plus(Duration.ofHours(1)));
        assertThat(task.getLastSuccess()).isEqualTo(NOW.minus(Duration.ofHours(2)));
        assertThat(task.getLastFailure()).isEqualTo(NOW.minus(Duration.ofDays(1)));
        assertThat(task.getMaxConsecutiveFailures()).isEqualTo(2);
      });
  }

  @Test
  void getOverviewTasks_marksRecurringFromTaskDefinitions() {
    StubSchedulerClient scheduler = new StubSchedulerClient(
      List.of(summary("heartbeat", 1, 0, 0, 1, NOW, null, null, 0))
    );

    List<OverviewTask> tasks = new OverviewService(scheduler, List.of(recurringTask("heartbeat"))).getOverviewTasks();

    assertThat(tasks)
      .singleElement()
      .satisfies(task -> {
        assertThat(task.getTaskName()).isEqualTo("heartbeat");
        assertThat(task.getRecurring()).isTrue();
        assertThat(task.getWorstStatus()).isEqualTo(OverviewTaskStatus.SCHEDULED);
      });
  }

  @Test
  void getOverviewTasks_addsDormantRowsForUnscheduledOneTimeDefinitionsOnly() {
    StubSchedulerClient scheduler = new StubSchedulerClient(List.of());

    List<OverviewTask> tasks = new OverviewService(
      scheduler,
      List.of(oneTimeTask("manual"), recurringTask("heartbeat"))
    ).getOverviewTasks();

    assertThat(tasks)
      .singleElement()
      .satisfies(task -> {
        assertThat(task.getTaskName()).isEqualTo("manual");
        assertThat(task.getRecurring()).isFalse();
        assertThat(task.getInstanceCount()).isZero();
        assertThat(task.getWorstStatus()).isEqualTo(OverviewTaskStatus.DORMANT);
      });
  }

  @Test
  void getOverviewTasks_omitsDormantRowsForRecurringTasksWithPersistentSchedule() {
    StubSchedulerClient scheduler = new StubSchedulerClient(List.of());

    List<OverviewTask> tasks = new OverviewService(
      scheduler,
      List.of(recurringWithPersistentScheduleTask("dynamic"))
    ).getOverviewTasks();

    assertThat(tasks).isEmpty();
  }

  @Test
  void getOverviewTasks_degradesWhenTaskDefinitionsAreUnavailable() {
    StubSchedulerClient scheduler = new StubSchedulerClient(
      List.of(summary("unknown", 1, 0, 0, 1, NOW, null, null, 0))
    );

    List<OverviewTask> tasks = new OverviewService(scheduler).getOverviewTasks();

    assertThat(tasks)
      .singleElement()
      .satisfies(task -> {
        assertThat(task.getTaskName()).isEqualTo("unknown");
        assertThat(task.getRecurring()).isNull();
        assertThat(task.getWorstStatus()).isEqualTo(OverviewTaskStatus.SCHEDULED);
      });
  }

  private static Task<Void> recurringTask(String name) {
    return Tasks.recurring(name, Schedules.fixedDelay(Duration.ofHours(1))).execute(
      (taskInstance, executionContext) -> {}
    );
  }

  private static Task<Void> oneTimeTask(String name) {
    return Tasks.oneTime(name).execute((taskInstance, executionContext) -> {});
  }

  private static Task<ScheduleAndData> recurringWithPersistentScheduleTask(String name) {
    return Tasks.recurringWithPersistentSchedule(name, ScheduleAndData.class).execute(
      (taskInstance, executionContext) -> {}
    );
  }

  private static TaskSummary summary(
    String taskName,
    int instanceCount,
    int runningCount,
    int failingCount,
    int scheduledCount,
    Instant earliestExecutionTime,
    Instant latestLastSuccess,
    Instant latestLastFailure,
    int maxConsecutiveFailures
  ) {
    return new TaskSummary(
      taskName,
      instanceCount,
      runningCount,
      failingCount,
      scheduledCount,
      earliestExecutionTime,
      latestLastSuccess,
      latestLastFailure,
      maxConsecutiveFailures
    );
  }
}
