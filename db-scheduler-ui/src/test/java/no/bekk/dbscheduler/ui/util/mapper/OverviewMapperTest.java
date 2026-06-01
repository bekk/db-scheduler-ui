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

import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.task.Execution;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import java.time.Instant;
import java.util.List;
import no.bekk.dbscheduler.ui.model.OverviewTask;
import no.bekk.dbscheduler.ui.model.WorstStatus;
import org.junit.jupiter.api.Test;

class OverviewMapperTest {

  private static final Instant NOW = Instant.parse("2026-06-01T12:00:00Z");

  private static ScheduledExecution<Object> exec(
      String taskName,
      String id,
      Instant executionTime,
      boolean picked,
      Instant lastSuccess,
      Instant lastFailure,
      int consecutiveFailures) {
    TaskInstance<Object> taskInstance = new TaskInstance<>(taskName, id);
    Execution execution =
        new Execution(
            executionTime,
            taskInstance,
            picked,
            picked ? "scheduler-1" : null,
            lastSuccess,
            lastFailure,
            consecutiveFailures,
            null,
            1L);
    return new ScheduledExecution<>(Object.class, execution);
  }

  @Test
  void aggregatesSingleScheduledTask() {
    Instant next = NOW.plusSeconds(3600);
    List<OverviewTask> result =
        OverviewMapper.aggregate(List.of(exec("task-a", "1", next, false, null, null, 0)));

    assertThat(result).hasSize(1);
    OverviewTask t = result.get(0);
    assertThat(t.getTaskName()).isEqualTo("task-a");
    assertThat(t.getInstanceCount()).isEqualTo(1);
    assertThat(t.getWorstStatus()).isEqualTo(WorstStatus.SCHEDULED);
    assertThat(t.getCounts().getScheduled()).isEqualTo(1);
    assertThat(t.getCounts().getRunning()).isZero();
    assertThat(t.getCounts().getFailing()).isZero();
    assertThat(t.getNextExecutionTime()).isEqualTo(next);
    assertThat(t.getRunningSince()).isNull();
    assertThat(t.getRecurring()).isNull(); // overlaid later by TaskLogic
  }

  @Test
  void runningInstanceContributesRunningSinceNotNextExecutionTime() {
    Instant firedAt = NOW.minusSeconds(120);
    List<OverviewTask> result =
        OverviewMapper.aggregate(
            List.of(exec("task-run", "1", firedAt, true, NOW.minusSeconds(600), null, 0)));

    OverviewTask t = result.get(0);
    assertThat(t.getWorstStatus()).isEqualTo(WorstStatus.RUNNING);
    assertThat(t.getCounts().getRunning()).isEqualTo(1);
    assertThat(t.getRunningSince()).isEqualTo(firedAt);
    assertThat(t.getNextExecutionTime()).isNull();
  }

  @Test
  void pickedAndFailingIsCountedAsFailing() {
    Instant firedAt = NOW.minusSeconds(30);
    List<OverviewTask> result =
        OverviewMapper.aggregate(List.of(exec("task-x", "1", firedAt, true, null, NOW, 3)));

    OverviewTask t = result.get(0);
    assertThat(t.getWorstStatus()).isEqualTo(WorstStatus.FAILING);
    assertThat(t.getCounts().getFailing()).isEqualTo(1);
    assertThat(t.getCounts().getRunning()).isZero();
    assertThat(t.getMaxConsecutiveFailures()).isEqualTo(3);
    // picked execution's firing time goes to runningSince, never nextExecutionTime
    assertThat(t.getRunningSince()).isEqualTo(firedAt);
    assertThat(t.getNextExecutionTime()).isNull();
  }

  @Test
  void multiInstanceBucketsAndAggregatesTimes() {
    Instant soonestScheduled = NOW.plusSeconds(12);
    Instant olderSuccess = NOW.minusSeconds(900);
    Instant newerSuccess = NOW.minusSeconds(120);
    Instant olderFailure = NOW.minusSeconds(800);
    Instant newerFailure = NOW.minusSeconds(60);

    List<OverviewTask> result =
        OverviewMapper.aggregate(
            List.of(
                exec("multi", "fail-1", NOW.plusSeconds(300), false, olderSuccess, newerFailure, 2),
                exec("multi", "run-1", NOW.minusSeconds(40), true, newerSuccess, olderFailure, 0),
                exec("multi", "sched-1", soonestScheduled, false, null, null, 0),
                exec("multi", "sched-2", NOW.plusSeconds(600), false, null, null, 0)));

    assertThat(result).hasSize(1);
    OverviewTask t = result.get(0);
    assertThat(t.getInstanceCount()).isEqualTo(4);
    assertThat(t.getWorstStatus()).isEqualTo(WorstStatus.FAILING);
    assertThat(t.getCounts().getFailing()).isEqualTo(1);
    assertThat(t.getCounts().getRunning()).isEqualTo(1);
    assertThat(t.getCounts().getScheduled()).isEqualTo(2);
    assertThat(t.getMaxConsecutiveFailures()).isEqualTo(2);
    // soonest across NON-picked (excludes the running instance's past firing time)
    assertThat(t.getNextExecutionTime()).isEqualTo(soonestScheduled);
    assertThat(t.getRunningSince()).isEqualTo(NOW.minusSeconds(40));
    // most-recent across instances
    assertThat(t.getLastSuccess()).isEqualTo(newerSuccess);
    assertThat(t.getLastFailure()).isEqualTo(newerFailure);
  }

  @Test
  void deduplicatesRepeatedPickedExecutions() {
    Instant firedAt = NOW.minusSeconds(10);
    // Same (taskName, id) appears twice — mirrors Caching adding withPicked(true) on top of the
    // unfiltered fetch. Must be counted once.
    List<OverviewTask> result =
        OverviewMapper.aggregate(
            List.of(
                exec("dup", "1", firedAt, true, null, null, 0),
                exec("dup", "1", firedAt, true, null, null, 0)));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getInstanceCount()).isEqualTo(1);
    assertThat(result.get(0).getCounts().getRunning()).isEqualTo(1);
  }
}
