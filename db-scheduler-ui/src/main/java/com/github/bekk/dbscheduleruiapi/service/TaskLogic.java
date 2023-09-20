package com.github.bekk.dbscheduleruiapi.service;

import com.github.bekk.dbscheduleruiapi.model.*;
import com.github.bekk.dbscheduleruiapi.util.Caching;
import com.github.bekk.dbscheduleruiapi.util.QueryUtils;
import com.github.bekk.dbscheduleruiapi.util.mapper.TaskMapper;
import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.TaskInstanceId;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskLogic {

  private final Scheduler scheduler;
  private final Caching caching;

  @Autowired
  public TaskLogic(Scheduler scheduler, Caching caching) {
    this.scheduler = scheduler;
    this.scheduler.start();
    this.caching = caching;
  }

  public void runTaskNow(String taskId, String taskName) {
    Optional<ScheduledExecution<Object>> scheduledExecutionOpt =
        scheduler.getScheduledExecution(TaskInstanceId.of(taskName, taskId));

    if (scheduledExecutionOpt.isPresent()) {
      TaskInstanceId taskInstance = scheduledExecutionOpt.get().getTaskInstance();
      scheduler.reschedule(taskInstance, Instant.now());
    } else {
      // Handle the case where the ScheduledExecution is not found
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "No ScheduledExecution found for taskName: " + taskName + ", taskId: " + taskId);
    }
  }

  public void deleteTask(String taskId, String taskName) {
    Optional<ScheduledExecution<Object>> scheduledExecutionOpt =
        scheduler.getScheduledExecution(TaskInstanceId.of(taskName, taskId));

    if (scheduledExecutionOpt.isPresent()) {
      TaskInstanceId taskInstance = scheduledExecutionOpt.get().getTaskInstance();
      scheduler.cancel(taskInstance);
    } else {
      // Handle the case where the ScheduledExecution is not found
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "No ScheduledExecution found for taskName: " + taskName + ", taskId: " + taskId);
    }
  }

  public GetTasksResponse getAllTasks(TaskRequestParams params) {
    List<TaskModel> tasks =
        TaskMapper.mapAllExecutionsToTaskModel(
            caching.getExecutionsFromCacheOrDB(params.isRefresh(), scheduler));
    tasks =
        QueryUtils.sortTasks(
            QueryUtils.filterTasks(
                QueryUtils.search(tasks, params.getSearchTerm()), params.getFilter()),
            params.getSorting(),
            params.isAsc());
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
    tasks = QueryUtils.search(tasks, params.getSearchTerm());
    tasks =
        QueryUtils.sortTasks(
            QueryUtils.filterTasks(tasks, params.getFilter()), params.getSorting(), params.isAsc());
    List<TaskModel> pagedTasks =
        QueryUtils.paginate(tasks, params.getPageNumber(), params.getSize());
    return new GetTasksResponse(tasks.size(), pagedTasks, params.getSize());
  }

  public PollResponse pollTasks(TaskRequestParams params) {
    // Fetch all tasks from the database
    List<ScheduledExecution<Object>> allTasks = caching.getExecutionsFromDBWithoutUpdatingCache(scheduler);

    int newFailures = 0;
    int newRunning = 0;
    int newTasks = 0;

    for (ScheduledExecution<Object> task : allTasks) {
      boolean isFailed = task.getConsecutiveFailures() > 0;
      boolean isRunning = task.getPickedBy()!=null;

      if (!caching.isTaskInCache(task.getTaskInstance(), isFailed ? "FAILED" : "NOT_FAILED")) {
        newTasks++;
        if (isFailed) {
          newFailures++;
        }
        if (isRunning) {
          newRunning++;
        }
      }
    }

    return new PollResponse(newFailures, newRunning, newTasks);
  }
}
