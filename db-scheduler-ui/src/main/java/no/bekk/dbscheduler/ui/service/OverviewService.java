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

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.TaskSummary;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTaskWithPersistentSchedule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import no.bekk.dbscheduler.ui.model.OverviewTask;
import no.bekk.dbscheduler.ui.model.OverviewTaskCounts;
import no.bekk.dbscheduler.ui.model.OverviewTaskStatus;

/**
 * Builds the task-centric overview by delegating aggregation to db-scheduler's {@link
 * SchedulerClient#getScheduledExecutionsSummaryByTask()}, which performs the grouping and counting
 * in a single {@code GROUP BY task_name} query. The summary covers every task that currently has
 * scheduled executions; this class only enriches it with information db-scheduler does not track:
 * whether a task is recurring, and dormant rows for one-time tasks that have no live execution.
 */
public class OverviewService {

  private final SchedulerClient scheduler;
  private final List<Task<?>> taskDefinitions;
  private final boolean taskDefinitionsAvailable;

  public OverviewService(SchedulerClient scheduler) {
    this(scheduler, null);
  }

  public OverviewService(SchedulerClient scheduler, Collection<Task<?>> taskDefinitions) {
    this.scheduler = scheduler;
    this.taskDefinitionsAvailable = taskDefinitions != null && !taskDefinitions.isEmpty();
    this.taskDefinitions =
        taskDefinitions == null
            ? List.of()
            : taskDefinitions.stream()
                .sorted(Comparator.comparing(Task::getName))
                .collect(Collectors.toList());
  }

  public List<OverviewTask> getOverviewTasks() {
    List<TaskSummary> summaries = scheduler.getScheduledExecutionsSummaryByTask();

    Set<String> recurringTaskNames =
        taskDefinitions.stream()
            .filter(this::isRecurringTaskDefinition)
            .map(Task::getName)
            .collect(Collectors.toSet());
    Set<String> summarizedTaskNames =
        summaries.stream().map(TaskSummary::taskName).collect(Collectors.toSet());

    List<OverviewTask> overviewTasks = new ArrayList<>();
    summaries.forEach(summary -> overviewTasks.add(toOverviewTask(summary, recurringTaskNames)));

    if (taskDefinitionsAvailable) {
      taskDefinitions.stream()
          .filter(task -> !recurringTaskNames.contains(task.getName()))
          .filter(task -> !summarizedTaskNames.contains(task.getName()))
          .map(this::dormantTask)
          .forEach(overviewTasks::add);
    }

    overviewTasks.sort(Comparator.comparing(OverviewTask::taskName));
    return overviewTasks;
  }

  private OverviewTask toOverviewTask(TaskSummary summary, Set<String> recurringTaskNames) {
    OverviewTaskCounts counts =
        new OverviewTaskCounts(
            summary.failingCount(), summary.runningCount(), summary.scheduledCount());
    return new OverviewTask(
        summary.taskName(),
        taskDefinitionsAvailable ? recurringTaskNames.contains(summary.taskName()) : null,
        summary.instanceCount(),
        counts,
        worstStatus(counts),
        summary.earliestExecutionTime(),
        summary.latestLastSuccess(),
        summary.latestLastFailure(),
        summary.maxConsecutiveFailures());
  }

  private OverviewTask dormantTask(Task<?> task) {
    return new OverviewTask(
        task.getName(),
        false,
        0,
        new OverviewTaskCounts(0, 0, 0),
        OverviewTaskStatus.DORMANT,
        null,
        null,
        null,
        0);
  }

  private OverviewTaskStatus worstStatus(OverviewTaskCounts counts) {
    if (counts.failing() > 0) {
      return OverviewTaskStatus.FAILING;
    }
    if (counts.running() > 0) {
      return OverviewTaskStatus.RUNNING;
    }
    return OverviewTaskStatus.SCHEDULED;
  }

  private boolean isRecurringTaskDefinition(Task<?> task) {
    return task instanceof RecurringTask<?>
        || task instanceof RecurringTaskWithPersistentSchedule<?>;
  }
}
