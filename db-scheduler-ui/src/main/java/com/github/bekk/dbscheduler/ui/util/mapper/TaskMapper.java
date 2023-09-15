package com.github.bekk.dbscheduler.ui.util.mapper;

import com.github.bekk.dbscheduler.ui.model.TaskModel;
import com.github.kagkarlsson.scheduler.ScheduledExecution;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TaskMapper {
  public static List<TaskModel> mapScheduledExecutionsToTaskModel(
      List<ScheduledExecution<Object>> scheduledExecutions) {
    return scheduledExecutions.stream()
        .map(
            execution ->
                new TaskModel(
                    execution.getTaskInstance().getTaskName(),
                    Collections.singletonList(execution.getTaskInstance().getId()),
                    Collections.singletonList(execution.getData()),
                    Collections.singletonList(execution.getExecutionTime()),
                    List.of(execution.isPicked()),
                    Collections.singletonList(execution.getPickedBy()),
                    Collections.singletonList(execution.getLastSuccess()),
                    execution.getLastFailure(),
                    List.of(execution.getConsecutiveFailures()), // Modified here
                    null,
                    0))
        .collect(Collectors.toList());
  }

  public static List<TaskModel> groupTasks(List<TaskModel> tasks) {
    return tasks.stream().collect(Collectors.groupingBy(TaskModel::getTaskName)).values().stream()
        .map(
            taskModels -> {
              TaskModel taskModel = taskModels.get(0);
              taskModel.setTaskInstance(
                  taskModels.stream()
                      .map(TaskModel::getTaskInstance)
                      .flatMap(List::stream)
                      .collect(Collectors.toList()));
              taskModel.setTaskData(
                  taskModels.stream()
                      .map(TaskModel::getTaskData)
                      .flatMap(List::stream)
                      .collect(Collectors.toList()));
              taskModel.setExecutionTime(
                  taskModels.stream()
                      .map(TaskModel::getExecutionTime)
                      .flatMap(List::stream)
                      .collect(Collectors.toList()));
              taskModel.setPicked(
                  taskModels.stream()
                      .map(TaskModel::isPicked)
                      .flatMap(List::stream)
                      .collect(Collectors.toList()));
              taskModel.setPickedBy(
                  taskModels.stream()
                      .map(TaskModel::getPickedBy)
                      .flatMap(List::stream)
                      .collect(Collectors.toList()));
              taskModel.setLastSuccess(
                  taskModels.stream()
                      .map(TaskModel::getLastSuccess)
                      .flatMap(List::stream)
                      .collect(Collectors.toList()));
              taskModel.setLastFailure(
                  taskModels.stream()
                      .map(TaskModel::getLastFailure)
                      .filter(Objects::nonNull)
                      .findFirst()
                      .orElse(null));
              taskModel.setConsecutiveFailures(
                  taskModels.stream()
                      .map(TaskModel::getConsecutiveFailures)
                      .flatMap(List::stream)
                      .collect(Collectors.toList())); // Modified here
              return taskModel;
            })
        .collect(Collectors.toList());
  }

  public static List<TaskModel> mapAllExecutionsToTaskModel(
      List<ScheduledExecution<Object>> scheduledExecutions) {
    return groupTasks(mapScheduledExecutionsToTaskModel(scheduledExecutions));
  }

  public static List<TaskModel> mapAllExecutionsToTaskModelUngrouped(
      List<ScheduledExecution<Object>> scheduledExecutions) {
    return mapScheduledExecutionsToTaskModel(scheduledExecutions);
  }
}
