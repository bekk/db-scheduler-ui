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

import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.ScheduledExecutionsFilter;
import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.TaskInstanceId;
import java.util.List;
import java.util.Optional;
import no.bekk.dbscheduler.ui.model.InstanceDetail;
import no.bekk.dbscheduler.ui.model.InstanceHistory;
import no.bekk.dbscheduler.ui.model.InstanceStatus;

/**
 * Builds the detail view of a single execution.
 *
 * <p>Named executions are fetched by primary key ({@link
 * SchedulerClient#getScheduledExecution(TaskInstanceId)}); resolving a task's sole execution reads
 * at most two rows for that task.
 *
 * <p>The log side is optional: with {@code db-scheduler-ui.history} off there is no log table to
 * read, and the detail comes back with a {@code null} history.
 */
public class InstanceService {

  /** Enough to see a pattern (flapping, or failing since a deploy) without paging. */
  private static final int RECENT_RUNS = 5;

  private final SchedulerClient scheduler;
  private final InstanceLogRepository logRepository;
  private final boolean showTaskData;

  public InstanceService(
      SchedulerClient scheduler, InstanceLogRepository logRepository, boolean showTaskData) {
    this.scheduler = scheduler;
    this.logRepository = logRepository;
    this.showTaskData = showTaskData;
  }

  /** The named execution, or empty when it is not scheduled (it finished, or never existed). */
  public Optional<InstanceDetail> getInstance(String taskName, String instanceId) {
    return scheduler
        .getScheduledExecution(TaskInstanceId.of(taskName, instanceId))
        .map(this::toDetail);
  }

  /**
   * The task's only execution, together with how many it actually has.
   *
   * <p>The overview lists tasks, not executions, so a row there can name a task but not an
   * instance. When the task has exactly one execution the row unambiguously means that one. The
   * count comes back either way because "none" and "several" are different answers: one is a thing
   * that is gone, the other a question that was not specific enough.
   *
   * @param detail the execution, or {@code null} unless there is exactly one
   */
  public record SoleInstance(InstanceDetail detail, int executionCount) {}

  public SoleInstance getSoleInstance(String taskName) {
    // all() rather than the single-argument overload, which filters to picked=false and would
    // hide the execution while it runs. Two rows: one to show, two to detect ambiguity.
    List<ScheduledExecution<Object>> executions =
        scheduler.getScheduledExecutionsForTask(
            taskName, Object.class, ScheduledExecutionsFilter.all().limit(2));
    return new SoleInstance(
        executions.size() == 1 ? toDetail(executions.get(0)) : null, executions.size());
  }

  private InstanceDetail toDetail(ScheduledExecution<Object> execution) {
    String taskName = execution.getTaskInstance().getTaskName();
    String instanceId = execution.getTaskInstance().getId();

    return new InstanceDetail(
        taskName,
        instanceId,
        status(execution),
        execution.getExecutionTime(),
        execution.isPicked(),
        execution.getPickedBy(),
        execution.getLastSuccess(),
        execution.getLastFailure(),
        execution.getConsecutiveFailures(),
        showTaskData ? execution.getData() : null,
        history(taskName, instanceId, execution.getLastFailure() != null));
  }

  private InstanceHistory history(String taskName, String instanceId, boolean hasEverFailed) {
    if (logRepository == null) {
      return null;
    }
    // Skip the failure query when the execution never failed: with nothing to match, it cannot
    // stop early and scans the instance's whole log.
    return new InstanceHistory(
        hasEverFailed ? logRepository.findLastFailure(taskName, instanceId).orElse(null) : null,
        logRepository.findRecentRuns(taskName, instanceId, RECENT_RUNS));
  }

  // Picked beats failing: a failing execution that is running now reports as RUNNING.
  private InstanceStatus status(ScheduledExecution<Object> execution) {
    if (execution.isPicked()) {
      return InstanceStatus.RUNNING;
    }
    return execution.getConsecutiveFailures() > 0
        ? InstanceStatus.FAILED
        : InstanceStatus.SCHEDULED;
  }
}
