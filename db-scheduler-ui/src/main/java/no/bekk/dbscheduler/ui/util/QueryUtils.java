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
package no.bekk.dbscheduler.ui.util;

import com.github.kagkarlsson.scheduler.ScheduledExecution;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.bekk.dbscheduler.ui.model.TaskModel;
import no.bekk.dbscheduler.ui.model.TaskRequestParams;
import no.bekk.dbscheduler.ui.model.TaskRequestParams.TaskFilter;
import no.bekk.dbscheduler.ui.model.TaskRequestParams.TaskSort;

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

  public static List<ScheduledExecution<Object>> filterExecutions(
      List<ScheduledExecution<Object>> executions, TaskFilter filter, String taskName) {
    return executions.stream()
        .filter(
            execution -> {
              if (taskName != null && !taskName.equals(execution.getTaskInstance().getTaskName())) {
                return false;
              }
              switch (filter) {
                case FAILED:
                  return execution.getConsecutiveFailures() != 0;
                case RUNNING:
                  return execution.isPicked();
                case SCHEDULED:
                  return execution.getConsecutiveFailures() == 0 && !execution.isPicked();
                default:
                  return true;
              }
            })
        .collect(Collectors.toList());
  }

  public static List<TaskModel> sortTasks(List<TaskModel> tasks, TaskSort sortType, boolean isAsc) {
    if (sortType == TaskSort.NAME) {
      Comparator<TaskModel> compareTasks = Comparator.comparing(TaskModel::getTaskName);
      tasks.sort(isAsc ? compareTasks : compareTasks.reversed());
    } else if (sortType == TaskSort.DEFAULT) {
      Comparator<TaskModel> comparator =
          Comparator.comparing(
              task -> task.getExecutionTime().stream().min(Instant::compareTo).orElse(Instant.MAX),
              Comparator.nullsLast(Instant::compareTo));
      tasks.sort(isAsc ? comparator : comparator.reversed());
    }
    return tasks;
  }

  public static List<TaskModel> searchByTaskName(
      List<TaskModel> tasks, String searchTermTaskName, boolean isExactMatch) {
    return search(tasks, searchTermTaskName, true, isExactMatch);
  }

  public static List<TaskModel> searchByTaskInstance(
      List<TaskModel> tasks, String searchTermTaskInstance, boolean isExactMatch) {
    return search(tasks, searchTermTaskInstance, false, isExactMatch);
  }

  public static List<TaskModel> search(
      List<TaskModel> tasks, String searchTerm, boolean isTaskNameSearch, boolean isExactMatch) {
    if (searchTerm == null || searchTerm.trim().isEmpty()) {
      return tasks;
    }

    return tasks.stream()
        .filter(
            task -> {
              String lowerCaseTerm = searchTerm.toLowerCase();

              boolean isTermPresent;
              if (isTaskNameSearch) {
                isTermPresent =
                    isExactMatch
                        ? task.getTaskName().equalsIgnoreCase(lowerCaseTerm)
                        : task.getTaskName().toLowerCase().contains(lowerCaseTerm);
              } else {
                isTermPresent =
                    task.getTaskInstance().stream()
                        .anyMatch(
                            instance ->
                                instance != null
                                    && (isExactMatch
                                        ? instance.equalsIgnoreCase(lowerCaseTerm)
                                        : instance.toLowerCase().contains(lowerCaseTerm)));
              }

              return isTermPresent;
            })
        .collect(Collectors.toList());
  }

  public static String logSearchCondition(
      String searchTerm, Map<String, Object> params, boolean isTaskName, boolean isExactMatch) {
    StringBuilder conditions = new StringBuilder();
    List<String> termConditions = new ArrayList<>();
    String termKey = "searchTerm" + (isTaskName ? "TaskName" : "TaskInstance");
    params.put(termKey, isExactMatch ? searchTerm : "%" + searchTerm + "%");
    String condition =
        isTaskName
            ? "LOWER(task_name) " + (isExactMatch ? "=" : "LIKE") + " LOWER(:" + termKey + ")"
            : "LOWER(task_instance) " + (isExactMatch ? "=" : "LIKE") + " LOWER(:" + termKey + ")";
    termConditions.add(condition);

    return conditions.append(String.join(" AND ", termConditions)).toString();
  }
}
