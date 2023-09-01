package com.github.bekk.dbscheduleruiapi.util.mapper;

import com.github.bekk.dbscheduleruiapi.model.TaskModel;
import com.github.kagkarlsson.scheduler.CurrentlyExecuting;
import com.github.kagkarlsson.scheduler.ScheduledExecution;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TaskMapper {
    public static List<TaskModel> mapScheduledExecutionsToTaskModel(List<ScheduledExecution<Object>> scheduledExecutions) {
        return scheduledExecutions.stream()
                .map(execution -> new TaskModel(
                        execution.getTaskInstance().getTaskName(),
                        Arrays.asList(execution.getTaskInstance().getId()),
                        Arrays.asList(execution.getData()),
                        Arrays.asList(execution.getExecutionTime()),
                        Arrays.asList(execution.isPicked()),
                        Arrays.asList(execution.getPickedBy()),
                        Arrays.asList(execution.getLastSuccess()),
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
                        Arrays.asList(execution.getTaskInstance().getId()),
                        Arrays.asList(execution.getTaskInstance().getData()),
                        Arrays.asList(execution.getExecution().executionTime),
                        Arrays.asList(execution.getExecution().picked),
                        Arrays.asList(execution.getExecution().pickedBy),
                        Arrays.asList(execution.getExecution().lastSuccess),
                        execution.getExecution().lastFailure,
                        execution.getExecution().consecutiveFailures,
                        null,
                        0
                ))
                .collect(Collectors.toList());
    }

    public static List<TaskModel> groupTasks(List<TaskModel> tasks) {
        return tasks.stream()
                .collect(Collectors.groupingBy(TaskModel::getTaskName))
                .values()
                .stream()
                .map(taskModels -> {
                    TaskModel taskModel = taskModels.get(0);
                    taskModel.setTaskInstance(taskModels.stream().map(TaskModel::getTaskInstance).flatMap(List::stream).collect(Collectors.toList()));
                    taskModel.setTaskData(taskModels.stream().map(TaskModel::getTaskData).flatMap(List::stream).collect(Collectors.toList()));
                    taskModel.setExecutionTime(taskModels.stream().map(TaskModel::getExecutionTime).flatMap(List::stream).collect(Collectors.toList()));
                    taskModel.setPicked(taskModels.stream().map(TaskModel::isPicked).flatMap(List::stream).collect(Collectors.toList()));
                    taskModel.setPickedBy(taskModels.stream().map(TaskModel::getPickedBy).flatMap(List::stream).collect(Collectors.toList()));
                    taskModel.setLastSuccess(taskModels.stream().map(TaskModel::getLastSuccess).flatMap(List::stream).collect(Collectors.toList()));
                    taskModel.setLastFailure(taskModels.stream().map(TaskModel::getLastFailure).filter(x -> x != null).findFirst().orElse(null));
                    taskModel.setConsecutiveFailures(taskModels.stream().map(TaskModel::getConsecutiveFailures).max(Integer::compareTo).orElse(0));
                    return taskModel;
                })
                .collect(Collectors.toList());
    }
    
    
    public static List<TaskModel> mapAllExecutionsToTaskModel(List<ScheduledExecution<Object>> scheduledExecutions, List<CurrentlyExecuting> currentlyExecuting) {
        List<TaskModel> scheduled = groupTasks(mapScheduledExecutionsToTaskModel(scheduledExecutions));
        scheduled.addAll(groupTasks(mapCurrentlyExecutingToTaskModel(currentlyExecuting)));
        return scheduled;
    }
}