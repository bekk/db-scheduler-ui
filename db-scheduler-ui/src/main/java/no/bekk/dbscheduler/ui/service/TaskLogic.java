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

import static no.bekk.dbscheduler.ui.util.QueryUtils.filterExecutions;

import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskInstanceId;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import no.bekk.dbscheduler.ui.model.GetTasksResponse;
import no.bekk.dbscheduler.ui.model.OverviewCounts;
import no.bekk.dbscheduler.ui.model.OverviewTask;
import no.bekk.dbscheduler.ui.model.PollResponse;
import no.bekk.dbscheduler.ui.model.TaskDetailsRequestParams;
import no.bekk.dbscheduler.ui.model.TaskModel;
import no.bekk.dbscheduler.ui.model.TaskRequestParams;
import no.bekk.dbscheduler.ui.model.WorstStatus;
import no.bekk.dbscheduler.ui.util.Caching;
import no.bekk.dbscheduler.ui.util.QueryUtils;
import no.bekk.dbscheduler.ui.util.mapper.OverviewMapper;
import no.bekk.dbscheduler.ui.util.mapper.TaskMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class TaskLogic {

  private final Scheduler scheduler;
  private final Caching caching;
  private final boolean showData;

  /** Names of registered definitions that are {@code instanceof RecurringTask}. */
  private final Set<String> recurringTaskNames;

  /** All registered task-definition names. Empty ⇒ degraded mode (no definitions available). */
  private final Set<String> registeredTaskNames;

  public TaskLogic(Scheduler scheduler, Caching caching, boolean showData) {
    this(scheduler, caching, showData, List.of());
  }

  public TaskLogic(
      Scheduler scheduler, Caching caching, boolean showData, List<Task<?>> registeredTasks) {
    this.scheduler = scheduler;
    this.caching = caching;
    this.showData = showData;
    this.registeredTaskNames =
        registeredTasks.stream().map(Task::getName).collect(Collectors.toSet());
    this.recurringTaskNames =
        registeredTasks.stream()
            .filter(task -> task instanceof RecurringTask)
            .map(Task::getName)
            .collect(Collectors.toSet());
  }

  public void runTaskNow(String taskId, String taskName, Instant scheduleTime) {
    Optional<ScheduledExecution<Object>> scheduledExecutionOpt =
        scheduler.getScheduledExecution(TaskInstanceId.of(taskName, taskId));

    if (scheduledExecutionOpt.isPresent() && !scheduledExecutionOpt.get().isPicked()) {
      scheduler.reschedule(
          scheduledExecutionOpt.get().getTaskInstance(),
          scheduleTime != null ? scheduleTime : Instant.now());
    } else {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "No ScheduledExecution found for taskName: " + taskName + ", taskId: " + taskId);
    }
  }

  public void runTaskGroupNow(String taskName, boolean onlyFailed) {
    caching
        .getExecutionsFromCacheOrDB(false, scheduler)
        .forEach(
            (execution) -> {
              if ((!onlyFailed || execution.getConsecutiveFailures() > 0)
                  && taskName.equals(execution.getTaskInstance().getTaskName())) {
                try {
                  runTaskNow(
                      execution.getTaskInstance().getId(),
                      execution.getTaskInstance().getTaskName(),
                      Instant.now());
                } catch (ResponseStatusException e) {
                  System.out.println("Failed to run task: " + e.getMessage());
                }
              }
            });
  }

  public void deleteTask(String taskId, String taskName) {
    Optional<ScheduledExecution<Object>> scheduledExecutionOpt =
        scheduler.getScheduledExecution(TaskInstanceId.of(taskName, taskId));

    if (scheduledExecutionOpt.isPresent()) {
      TaskInstanceId taskInstance = scheduledExecutionOpt.get().getTaskInstance();
      scheduler.cancel(taskInstance);
    } else {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "No ScheduledExecution found for taskName: " + taskName + ", taskId: " + taskId);
    }
  }

  public GetTasksResponse getAllTasks(TaskRequestParams params) {
    List<ScheduledExecution<Object>> executions =
        caching.getExecutionsFromCacheOrDB(params.isRefresh(), scheduler);

    List<TaskModel> tasks = TaskMapper.mapAllExecutionsToTaskModelUngrouped(executions);

    tasks =
        QueryUtils.searchByTaskName(
            tasks, params.getSearchTermTaskName(), params.isTaskNameExactMatch());
    tasks =
        QueryUtils.searchByTaskInstance(
            tasks, params.getSearchTermTaskInstance(), params.isTaskInstanceExactMatch());
    if (!showData) {
      tasks.forEach(e -> e.setTaskData(List.of()));
    }
    tasks = TaskMapper.groupTasks(tasks);
    tasks =
        QueryUtils.sortTasks(
            QueryUtils.filterTasks(tasks, params.getFilter()), params.getSorting(), params.isAsc());
    List<TaskModel> pagedTasks =
        QueryUtils.paginate(tasks, params.getPageNumber(), params.getSize());
    return new GetTasksResponse(tasks.size(), pagedTasks, params.getSize());
  }

  public GetTasksResponse getTask(TaskDetailsRequestParams params) {
    List<ScheduledExecution<Object>> executions =
        caching.getExecutionsFromCacheOrDB(params.isRefresh(), scheduler);

    List<TaskModel> tasks =
        params.getTaskId() != null
            ? TaskMapper.mapAllExecutionsToTaskModelUngrouped(executions).stream()
                .filter(
                    task -> {
                      return task.getTaskName().equals(params.getTaskName())
                          && task.getTaskInstance().get(0).equals(params.getTaskId());
                    })
                .collect(Collectors.toList())
            : TaskMapper.mapAllExecutionsToTaskModelUngrouped(executions).stream()
                .filter(
                    task -> {
                      return task.getTaskName().equals(params.getTaskName());
                    })
                .collect(Collectors.toList());
    if (tasks.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "No tasks found for taskName: "
              + params.getTaskName()
              + ", taskId: "
              + params.getTaskId());
    }
    tasks =
        QueryUtils.searchByTaskName(
            tasks, params.getSearchTermTaskName(), params.isTaskNameExactMatch());
    tasks =
        QueryUtils.searchByTaskInstance(
            tasks, params.getSearchTermTaskInstance(), params.isTaskInstanceExactMatch());
    tasks =
        QueryUtils.sortTasks(
            QueryUtils.filterTasks(tasks, params.getFilter()), params.getSorting(), params.isAsc());
    if (!showData) {
      List<Object> list =
          new ArrayList<>() {
            {
              add(null);
            }
          };
      tasks.forEach(e -> e.setTaskData(list));
    }

    List<TaskModel> pagedTasks =
        QueryUtils.paginate(tasks, params.getPageNumber(), params.getSize());
    return new GetTasksResponse(tasks.size(), pagedTasks, params.getSize());
  }

  /**
   * Task-centric Overview: one {@link OverviewTask} per task name, name-sorted. Aggregates over the
   * scheduled executions (Java-side group-by via {@link OverviewMapper}) and overlays the {@code
   * recurring} flag plus dormant rows from the registered task definitions. When no task
   * definitions are available (degraded mode), {@code recurring} is left {@code null} and no
   * dormant rows are emitted.
   */
  public List<OverviewTask> getOverview(boolean refresh) {
    List<ScheduledExecution<Object>> executions =
        caching.getExecutionsFromCacheOrDB(refresh, scheduler);

    List<OverviewTask> tasks = OverviewMapper.aggregate(executions);

    boolean degraded = registeredTaskNames.isEmpty();
    if (!degraded) {
      Set<String> namesWithExecutions = new HashSet<>();
      for (OverviewTask task : tasks) {
        task.setRecurring(recurringTaskNames.contains(task.getTaskName()));
        namesWithExecutions.add(task.getTaskName());
      }
      // Dormant rows: registered one-time/dynamic/custom definitions with no scheduled executions.
      // Recurring (RecurringTask) definitions are never shown as dormant — a recurring task with 0
      // executions is an abnormal state, out of scope here.
      for (String name : registeredTaskNames) {
        if (!namesWithExecutions.contains(name) && !recurringTaskNames.contains(name)) {
          tasks.add(dormantTask(name));
        }
      }
    }

    tasks.sort(Comparator.comparing(OverviewTask::getTaskName));
    return tasks;
  }

  private static OverviewTask dormantTask(String taskName) {
    return OverviewTask.builder()
        .taskName(taskName)
        .recurring(false)
        .instanceCount(0)
        .counts(new OverviewCounts(0, 0, 0))
        .worstStatus(WorstStatus.DORMANT)
        .maxConsecutiveFailures(0)
        .build();
  }

  public PollResponse pollTasks(TaskDetailsRequestParams params) {
    List<ScheduledExecution<Object>> allTasks =
        filterExecutions(
            caching.getExecutionsFromDBWithoutUpdatingCache(scheduler),
            TaskRequestParams.TaskFilter.ALL,
            params.getTaskName());

    Set<String> newTaskNames = new HashSet<>();
    Set<String> newFailureTaskNames = new HashSet<>();
    Set<String> newRunningTaskNames = new HashSet<>();

    int stoppedFailing = 0;
    int finishedRunning = 0;

    for (ScheduledExecution<Object> task : allTasks) {
      String taskName = task.getTaskInstance().getTaskName();
      String status = getStatus(task);
      String cachedStatus = caching.getStatusFromCache(task.getTaskInstance());

      if (cachedStatus == null) {
        handleNewTask(
            params, newTaskNames, newFailureTaskNames, newRunningTaskNames, taskName, status);
        continue;
      }

      if (!cachedStatus.equals(status)) {
        handleStatusChange(
            params,
            newFailureTaskNames,
            newRunningTaskNames,
            taskName,
            status,
            cachedStatus,
            stoppedFailing,
            finishedRunning);
      }
    }

    return new PollResponse(
        newFailureTaskNames.size(),
        newRunningTaskNames.size(),
        newTaskNames.size(),
        stoppedFailing,
        finishedRunning);
  }

  private void handleNewTask(
      TaskDetailsRequestParams params,
      Set<String> newTaskNames,
      Set<String> newFailureTaskNames,
      Set<String> newRunningTaskNames,
      String taskName,
      String status) {
    if (newTaskNames.contains(taskName) && params.getTaskName() == null) {
      return;
    }

    newTaskNames.add(taskName);
    if (status.charAt(0) == '1') {
      newFailureTaskNames.add(taskName);
    }
    if (status.charAt(1) == '1') {
      newRunningTaskNames.add(taskName);
    }
  }

  private void handleStatusChange(
      TaskDetailsRequestParams params,
      Set<String> newFailureTaskNames,
      Set<String> newRunningTaskNames,
      String taskName,
      String status,
      String cachedStatus,
      int stoppedFailing,
      int finishedRunning) {
    if (cachedStatus.charAt(0) == '0'
        && status.charAt(0) == '1'
        && (!newFailureTaskNames.contains(taskName) || params.getTaskName() != null)) {
      newFailureTaskNames.add(taskName);
    }
    if (cachedStatus.charAt(0) == '1' && status.charAt(0) == '0') {
      stoppedFailing++;
    }

    if (cachedStatus.charAt(1) == '0'
        && status.charAt(1) == '1'
        && (!newRunningTaskNames.contains(taskName) || params.getTaskName() != null)) {
      newRunningTaskNames.add(taskName);
    }
    if (cachedStatus.charAt(1) == '1' && status.charAt(1) == '0') {
      finishedRunning++;
    }
  }

  private String getStatus(ScheduledExecution<Object> task) {
    return (task.getConsecutiveFailures() > 0 ? "1" : "0")
        + (task.getPickedBy() != null ? "1" : "0");
  }
}
