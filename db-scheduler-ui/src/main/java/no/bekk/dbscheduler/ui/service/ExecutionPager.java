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

import com.github.kagkarlsson.scheduler.ExecutionTimeAndId;
import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.ScheduledExecutionsFilter;
import com.github.kagkarlsson.scheduler.SchedulerClient;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import no.bekk.dbscheduler.ui.model.ExecutionPage;
import no.bekk.dbscheduler.ui.model.ExecutionRow;
import no.bekk.dbscheduler.ui.model.ExecutionStatusFilter;
import no.bekk.dbscheduler.ui.model.InstanceStatus;
import no.bekk.dbscheduler.ui.util.TaskDataPreview;

/**
 * One page of a task's executions, as a single seeked query in {@code (execution_time,
 * task_instance)} order. Never loads more than one page plus one row, however many executions the
 * task has.
 */
public class ExecutionPager {

  static final int DEFAULT_LIMIT = 50;
  static final int MAX_LIMIT = 200;

  /** Lower bound for ranges that must have one; earlier than any execution time. */
  static final ExecutionTimeAndId START = new ExecutionTimeAndId(Instant.EPOCH, "");

  private final SchedulerClient scheduler;
  private final boolean showTaskData;
  private final Clock clock;

  public ExecutionPager(SchedulerClient scheduler, boolean showTaskData, Clock clock) {
    this.scheduler = scheduler;
    this.showTaskData = showTaskData;
    this.clock = clock;
  }

  /**
   * @param after cursor from the previous page; wins over {@code from}
   * @param from execution time to start at when there is no cursor
   * @throws IllegalArgumentException when {@code after} is not a cursor this class produced
   */
  public ExecutionPage page(
      String taskName, ExecutionStatusFilter status, String after, Instant from, Integer limit) {
    int size = limit == null ? DEFAULT_LIMIT : Math.max(1, Math.min(limit, MAX_LIMIT));
    ExecutionTimeAndId start = start(after, from);

    ScheduledExecutionsFilter filter = ScheduledExecutionsFilter.all();
    switch (status) {
      case FAILING -> filter.withPicked(false).withFailing(true);
      case SCHEDULED -> filter.withPicked(false).withFailing(false);
      case RUNNING -> filter.withPicked(true);
      // db-scheduler orders by descending time when given only an upper bound, so overdue
      // always carries a lower one too and stays ascending.
      case OVERDUE ->
          filter
              .withPicked(false)
              .after(start != null ? start : START)
              .before(new ExecutionTimeAndId(clock.instant(), ""));
      case ALL -> {}
    }
    if (status != ExecutionStatusFilter.OVERDUE && start != null) {
      filter.after(start);
    }
    filter.limit(size + 1);

    List<ScheduledExecution<Object>> executions =
        scheduler.getScheduledExecutionsForTask(taskName, Object.class, filter);
    boolean more = executions.size() > size;
    List<ScheduledExecution<Object>> shown = more ? executions.subList(0, size) : executions;

    return new ExecutionPage(
        shown.stream().map(this::toRow).toList(),
        more ? ExecutionTimeAndId.from(shown.get(size - 1)).toEncodedString() : null);
  }

  private static ExecutionTimeAndId start(String after, Instant from) {
    if (after != null) {
      try {
        return ExecutionTimeAndId.fromEncodedString(after);
      } catch (RuntimeException e) {
        throw new IllegalArgumentException("Not a valid cursor: " + after, e);
      }
    }
    return from != null ? new ExecutionTimeAndId(from, "") : null;
  }

  private ExecutionRow toRow(ScheduledExecution<Object> execution) {
    return new ExecutionRow(
        execution.getTaskInstance().getId(),
        InstanceStatus.of(execution),
        execution.getExecutionTime(),
        execution.isPicked(),
        execution.getPickedBy(),
        execution.getLastSuccess(),
        execution.getLastFailure(),
        execution.getConsecutiveFailures(),
        TaskDataPreview.of(execution.getData(), showTaskData));
  }
}
