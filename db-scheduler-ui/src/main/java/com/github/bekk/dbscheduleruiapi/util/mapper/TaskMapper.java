package com.github.bekk.dbscheduleruiapi.util.mapper;

import com.github.bekk.dbscheduleruiapi.model.TaskModel;
import com.github.kagkarlsson.scheduler.CurrentlyExecuting;
import com.github.kagkarlsson.scheduler.ScheduledExecution;

import java.util.List;
import java.util.stream.Collectors;

public class TaskMapper {
    public static List<TaskModel> mapScheduledExecutionsToTaskModel(List<ScheduledExecution<Object>> scheduledExecutions) {
        return scheduledExecutions.stream()
                .map(execution -> new TaskModel(
                        execution.getTaskInstance().getTaskName(),
                        execution.getTaskInstance().getId(),
                        execution.getData(),
                        execution.getExecutionTime(),
                        execution.isPicked(),
                        execution.getPickedBy(),
                        execution.getLastSuccess(),
                        execution.getLastFailure(),
                        execution.getConsecutiveFailures(),
                        null,
                        0
                ))
                .collect(Collectors.toList());
    }

    public static List<TaskModel> mapCurrentlyExecutingToTaskModel(List<CurrentlyExecuting> currentlyExecuting) {
        return currentlyExecuting.stream()
                .map(execution -> new TaskModel(
                        execution.getTaskInstance().getTaskName(),
                        execution.getTaskInstance().getId(),
                        execution.getTaskInstance().getData(),
                        execution.getExecution().executionTime,
                        execution.getExecution().picked,
                        execution.getExecution().pickedBy,
                        execution.getExecution().lastSuccess,
                        execution.getExecution().lastFailure,
                        execution.getExecution().consecutiveFailures,
                        null,
                        0
                ))
                .collect(Collectors.toList());
    }

    public static List<TaskModel> mapAllExecutionsToTaskModel(List<ScheduledExecution<Object>> scheduledExecutions, List<CurrentlyExecuting> currentlyExecuting) {
        List<TaskModel> scheduled = mapScheduledExecutionsToTaskModel(scheduledExecutions);
        scheduled.addAll(mapCurrentlyExecutingToTaskModel(currentlyExecuting));
        return scheduled;
    }
}
