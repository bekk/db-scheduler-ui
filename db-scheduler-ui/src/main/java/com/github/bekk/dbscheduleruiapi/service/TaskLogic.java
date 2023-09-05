package com.github.bekk.dbscheduleruiapi.service;

import com.github.bekk.dbscheduleruiapi.model.GetTasksResponse;
import com.github.bekk.dbscheduleruiapi.model.TaskDetailsRequestParams;
import com.github.bekk.dbscheduleruiapi.model.TaskModel;
import com.github.bekk.dbscheduleruiapi.model.TaskRequestParams;
import com.github.bekk.dbscheduleruiapi.util.TaskPagination;
import com.github.bekk.dbscheduleruiapi.util.mapper.TaskMapper;
import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.TaskInstanceId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskLogic {

    private final Scheduler scheduler;

    @Autowired
    public TaskLogic(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.scheduler.start();
    }

    public void runTaskNow(String taskId, String taskName) {
        Optional<ScheduledExecution<Object>> scheduledExecutionOpt = scheduler.getScheduledExecution(TaskInstanceId.of(taskName, taskId));

        if (scheduledExecutionOpt.isPresent()) {
            TaskInstanceId taskInstance = scheduledExecutionOpt.get().getTaskInstance();
            scheduler.reschedule(taskInstance, Instant.now());
        } else {
            // Handle the case where the ScheduledExecution is not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No ScheduledExecution found for taskName: " + taskName + ", taskId: " + taskId);
        }
    }

    public void deleteTask(String taskId, String taskName) {
        Optional<ScheduledExecution<Object>> scheduledExecutionOpt = scheduler.getScheduledExecution(TaskInstanceId.of(taskName, taskId));

        if (scheduledExecutionOpt.isPresent()) {
            TaskInstanceId taskInstance = scheduledExecutionOpt.get().getTaskInstance();
            scheduler.cancel(taskInstance);
        } else {
            // Handle the case where the ScheduledExecution is not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No ScheduledExecution found for taskName: " + taskName + ", taskId: " + taskId);
        }
    }

    public GetTasksResponse getAllTasks(TaskRequestParams params) {
        List<TaskModel> tasks = TaskMapper.mapAllExecutionsToTaskModel(scheduler.getScheduledExecutions(), scheduler.getCurrentlyExecuting()).stream().filter(task -> {
                switch (params.getFilter()){
                    case FAILED:
                        return task.getConsecutiveFailures() != 0;
                    case RUNNING:
                        return task.isPicked().get(0);
                    case SCHEDULED:
                        return !task.isPicked().get(0) && task.getConsecutiveFailures() == 0;
                    default:
                        return true;
                }
        }).collect(Collectors.toList());

        if (params.getSorting() == TaskRequestParams.TaskSort.NAME) {
            tasks.sort((task1, task2) -> {
                int comparisonResult = task1.getTaskName().compareTo(task2.getTaskName());
                return params.isAsc() ? comparisonResult : -comparisonResult;
            });
        }else if(params.getSorting() == TaskRequestParams.TaskSort.DEFAULT){
            Comparator<TaskModel> comparator = Comparator.comparing(
            task -> task.getExecutionTime().stream()
                        .min(Instant::compareTo)
                        .orElse(Instant.MAX),
            Comparator.nullsLast(Instant::compareTo));
            tasks.sort(params.isAsc()?comparator:comparator.reversed());
        }
        List<TaskModel> pagedTasks = TaskPagination.paginate(tasks, params.getPageNumber(), params.getSize());
        return new GetTasksResponse(tasks.size(), pagedTasks);

    }

    public GetTasksResponse getTask(TaskDetailsRequestParams params) {
        List<TaskModel> tasks = params.getTaskId()!=null
        ? TaskMapper.mapAllExecutionsToTaskModelUngrouped(scheduler.getScheduledExecutions(), scheduler.getCurrentlyExecuting()).stream().filter(task -> {
            return task.getTaskName().equals(params.getTaskName()) && task.getTaskInstance().get(0).equals(params.getTaskId());
        }).collect(Collectors.toList())
        : TaskMapper.mapAllExecutionsToTaskModelUngrouped(scheduler.getScheduledExecutions(), scheduler.getCurrentlyExecuting()).stream().filter(task -> {
            return task.getTaskName().equals(params.getTaskName());
        }).collect(Collectors.toList());
        if (tasks.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No ScheduledExecution found for taskName: "
                    + params.getTaskName() + ", taskId: " + params.getTaskId());
        }

        List<TaskModel> pagedTasks = TaskPagination.paginate(tasks, params.getPageNumber(), params.getSize());

        return new GetTasksResponse(tasks.size(), pagedTasks);
        }
}
