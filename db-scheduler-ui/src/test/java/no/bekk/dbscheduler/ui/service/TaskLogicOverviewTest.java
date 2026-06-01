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

import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.task.Execution;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import java.time.Instant;
import java.util.List;
import no.bekk.dbscheduler.ui.model.OverviewTask;
import no.bekk.dbscheduler.ui.model.WorstStatus;
import no.bekk.dbscheduler.ui.util.Caching;
import org.junit.jupiter.api.Test;

class TaskLogicOverviewTest {

  private static final Instant NOW = Instant.parse("2026-06-01T12:00:00Z");

  // Registered definitions. Names chosen so alphabetical order is deterministic.
  private static final Task<?> ALPHA_ONETIME = Tasks.oneTime("alpha-onetime").execute((i, c) -> {});
  private static final Task<?> BETA_RECURRING =
      Tasks.recurring("beta-recurring", FixedDelay.ofSeconds(10)).execute((i, c) -> {});
  private static final Task<?> GAMMA_DORMANT = Tasks.oneTime("gamma-dormant").execute((i, c) -> {});
  private static final Task<?> DELTA_RECURRING_EMPTY =
      Tasks.recurring("delta-recurring-empty", FixedDelay.ofSeconds(10)).execute((i, c) -> {});

  private static ScheduledExecution<Object> scheduledExec(String taskName, Instant time) {
    TaskInstance<Object> taskInstance = new TaskInstance<>(taskName, "1");
    Execution execution = new Execution(time, taskInstance, false, null, null, null, 0, null, 1L);
    return new ScheduledExecution<>(Object.class, execution);
  }

  private static Caching cacheWith(List<ScheduledExecution<Object>> executions) {
    Caching caching = new Caching();
    caching.updateCache(executions); // populate so getOverview(false) never touches the scheduler
    return caching;
  }

  private static OverviewTask byName(List<OverviewTask> tasks, String name) {
    return tasks.stream().filter(t -> t.getTaskName().equals(name)).findFirst().orElse(null);
  }

  @Test
  void overlaysRecurringFlagAndDormantRowsSortedByName() {
    Caching caching =
        cacheWith(
            List.of(
                scheduledExec("alpha-onetime", NOW.plusSeconds(60)),
                scheduledExec("beta-recurring", NOW.plusSeconds(60))));
    TaskLogic taskLogic =
        new TaskLogic(
            null,
            caching,
            false,
            List.of(ALPHA_ONETIME, BETA_RECURRING, GAMMA_DORMANT, DELTA_RECURRING_EMPTY));

    List<OverviewTask> result = taskLogic.getOverview(false);

    // One-time / recurring flags from the registered definitions.
    assertThat(byName(result, "alpha-onetime").getRecurring()).isFalse();
    assertThat(byName(result, "beta-recurring").getRecurring()).isTrue();
    assertThat(byName(result, "beta-recurring").getWorstStatus()).isNotEqualTo(WorstStatus.DORMANT);

    // A registered one-time definition with no executions is a dormant row.
    OverviewTask dormant = byName(result, "gamma-dormant");
    assertThat(dormant).isNotNull();
    assertThat(dormant.getWorstStatus()).isEqualTo(WorstStatus.DORMANT);
    assertThat(dormant.getRecurring()).isFalse();
    assertThat(dormant.getInstanceCount()).isZero();

    // A recurring definition with no executions is abnormal — never a dormant row, so absent.
    assertThat(byName(result, "delta-recurring-empty")).isNull();

    // Strictly alphabetical.
    assertThat(result)
        .extracting(OverviewTask::getTaskName)
        .containsExactly("alpha-onetime", "beta-recurring", "gamma-dormant");
  }

  @Test
  void degradedModeLeavesRecurringNullAndEmitsNoDormantRows() {
    Caching caching =
        cacheWith(
            List.of(
                scheduledExec("alpha-onetime", NOW.plusSeconds(60)),
                scheduledExec("beta-recurring", NOW.plusSeconds(60))));
    // No registered task definitions ⇒ degraded mode.
    TaskLogic taskLogic = new TaskLogic(null, caching, false);

    List<OverviewTask> result = taskLogic.getOverview(false);

    assertThat(result)
        .extracting(OverviewTask::getTaskName)
        .containsExactly("alpha-onetime", "beta-recurring");
    assertThat(result).allSatisfy(t -> assertThat(t.getRecurring()).isNull());
    assertThat(result).noneMatch(t -> t.getWorstStatus() == WorstStatus.DORMANT);
  }
}
