package com.github.bekk.dbscheduler.ui.util;

import com.github.bekk.dbscheduler.ui.model.TaskModel;
import com.github.bekk.dbscheduler.ui.model.TaskRequestParams;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class QueryUtils {

  public static <T> List<T> paginate(List<T> allItems, int pageNumber, int pageSize) {
    int startIndex = pageNumber * pageSize;
    int endIndex = Math.min(startIndex + pageSize, allItems.size());

    return (startIndex < endIndex) ? allItems.subList(startIndex, endIndex) : new ArrayList<>();
  }

  public static List<TaskModel> filterTasks(
      List<TaskModel> tasks, TaskRequestParams.TaskFilter filter) {
    return tasks.stream()
        .filter(
            task -> {
              switch (filter) {
                case FAILED:
                  return task.getConsecutiveFailures().stream().anyMatch(failures -> failures != 0);
                case RUNNING:
                  return task.isPicked().stream().anyMatch(Boolean::booleanValue);
                case SCHEDULED:
                  return IntStream.range(0, task.isPicked().size())
                      .anyMatch(
                          i ->
                              !task.isPicked().get(i) && task.getConsecutiveFailures().get(i) == 0);
                default:
                  return true;
              }
            })
        .collect(Collectors.toList());
  }

  public static List<TaskModel> sortTasks(
      List<TaskModel> tasks, TaskRequestParams.TaskSort sortType, boolean isAsc) {
    if (sortType == TaskRequestParams.TaskSort.NAME) {
      Comparator<TaskModel> compareTasks = Comparator.comparing(TaskModel::getTaskName);
      tasks.sort(isAsc ? compareTasks : compareTasks.reversed());
    } else if (sortType == TaskRequestParams.TaskSort.DEFAULT) {
      Comparator<TaskModel> comparator =
          Comparator.comparing(
              task -> task.getExecutionTime().stream().min(Instant::compareTo).orElse(Instant.MAX),
              Comparator.nullsLast(Instant::compareTo));
      tasks.sort(isAsc ? comparator : comparator.reversed());
    }
    return tasks;
  }
}
