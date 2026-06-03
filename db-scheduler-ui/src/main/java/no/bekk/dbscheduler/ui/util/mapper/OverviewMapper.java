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

import com.github.kagkarlsson.scheduler.TaskSummary;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import no.bekk.dbscheduler.ui.model.OverviewCounts;
import no.bekk.dbscheduler.ui.model.OverviewTask;
import no.bekk.dbscheduler.ui.model.WorstStatus;

/**
 * Builds the Overview rows by combining the DB-side per-task aggregation ({@link TaskSummary}, from
 * {@code SchedulerClient.getScheduledExecutionsSummaryByTask()}) with an overlay of registered task
 * definitions (for the {@code recurring} flag and dormant rows).
 *
 * <p>This is a deliberately separate path from the legacy {@code TaskMapper.groupTasks}, whose
 * {@code lastFailure} is the first non-null instance rather than the most recent.
 */
public final class OverviewMapper {

  private OverviewMapper() {}

  /**
   * @param summaries DB-side aggregation, one entry per task name that has at least one execution
   * @param recurringTaskNames names of registered {@code RecurringTask} definitions
   * @param allTaskNames names of all registered task definitions
   * @param taskDefinitionsKnown {@code false} in degraded mode (no registered definitions
   *     available) — then {@code recurring} is left {@code null} and no dormant rows are emitted
   */
  public static List<OverviewTask> toOverview(
      List<TaskSummary> summaries,
      Set<String> recurringTaskNames,
      Set<String> allTaskNames,
      boolean taskDefinitionsKnown) {

    List<OverviewTask> rows = new ArrayList<>(summaries.size());

    for (TaskSummary summary : summaries) {
      Boolean recurring =
          taskDefinitionsKnown ? recurringTaskNames.contains(summary.taskName()) : null;
      rows.add(
          new OverviewTask(
              summary.taskName(),
              recurring,
              summary.instanceCount(),
              new OverviewCounts(
                  summary.failingCount(), summary.runningCount(), summary.scheduledCount()),
              worstStatus(summary),
              summary.earliestExecutionTime(),
              summary.latestLastSuccess(),
              summary.latestLastFailure(),
              summary.maxConsecutiveFailures()));
    }

    if (taskDefinitionsKnown) {
      rows.addAll(dormantRows(summaries, recurringTaskNames, allTaskNames));
    }

    return rows;
  }

  /**
   * Dormant rows: registered <em>non-recurring</em> task definitions that currently have no
   * scheduled execution. Recurring tasks are never dormant — a recurring definition always
   * reschedules itself, so it always appears in the aggregation.
   */
  private static List<OverviewTask> dormantRows(
      List<TaskSummary> summaries, Set<String> recurringTaskNames, Set<String> allTaskNames) {
    Set<String> namesWithExecutions =
        summaries.stream().map(TaskSummary::taskName).collect(Collectors.toSet());

    List<OverviewTask> dormant = new ArrayList<>();
    for (String name : allTaskNames) {
      if (recurringTaskNames.contains(name) || namesWithExecutions.contains(name)) {
        continue;
      }
      dormant.add(
          new OverviewTask(
              name,
              false,
              0,
              new OverviewCounts(0, 0, 0),
              WorstStatus.DORMANT,
              null,
              null,
              null,
              0));
    }
    return dormant;
  }

  /**
   * Severity order: failing &gt; running &gt; scheduled.
   *
   * <p>"Failing" follows the spec's definition — <em>any</em> instance with {@code
   * consecutiveFailures > 0} — so we key off {@code maxConsecutiveFailures}, not {@code
   * failingCount}. The DB summary's {@code failingCount} only counts <em>un-picked</em> failing
   * instances ({@code picked = false AND consecutive_failures > 0}); an instance currently being
   * retried is {@code picked = true} and so lands in {@code runningCount}. Using {@code
   * maxConsecutiveFailures} ensures a task whose only failing instance is mid-retry is still shown
   * as FAILING rather than masked as RUNNING.
   */
  private static WorstStatus worstStatus(TaskSummary summary) {
    if (summary.maxConsecutiveFailures() > 0) {
      return WorstStatus.FAILING;
    }
    if (summary.runningCount() > 0) {
      return WorstStatus.RUNNING;
    }
    return WorstStatus.SCHEDULED;
  }
}
