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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

  public static List<TaskModel> search(List<TaskModel> tasks, String searchTerm) {
    if (searchTerm == null || searchTerm.trim().isEmpty()) {
      return tasks;
    }

    List<String> terms = splitSearchTerm(searchTerm);

    return tasks.stream()
        .filter(
            task -> {
              for (String term : terms) {
                String lowerCaseTerm = term.toLowerCase();
                boolean isTermInTaskName = task.getTaskName().toLowerCase().contains(lowerCaseTerm);
                boolean isTermInAnyTaskInstance =
                    task.getTaskInstance().stream()
                        .anyMatch(
                            instance ->
                                instance != null && instance.toLowerCase().contains(lowerCaseTerm));
                if (!isTermInTaskName && !isTermInAnyTaskInstance) {
                  return false;
                }
              }
              return true;
            })
        .collect(Collectors.toList());
  }

  public static String logSearchCondition(String searchTerm, Map<String, Object> params) {
    StringBuilder conditions = new StringBuilder();
    List<String> terms = splitSearchTerm(searchTerm);
    if (terms.size() > 0) {
      List<String> termConditions = new ArrayList<>();
      for (int i = 0; i < terms.size(); i++) {
        String termKey = "searchTerm" + i;
        termConditions.add(
            "(LOWER(task_name) LIKE LOWER(:"
                + termKey
                + ") OR LOWER(task_instance) LIKE LOWER(:"
                + termKey
                + "))");
        params.put(termKey, "%" + terms.get(i) + "%");
      }
      return conditions.append(String.join(" AND ", termConditions)).toString();
    }
    return "";
  }

  private static List<String> splitSearchTerm(String searchTerm) {
    List<String> terms = new ArrayList<>();
    Pattern pattern = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
    Matcher matcher = pattern.matcher(searchTerm);

    while (matcher.find()) {
      if (matcher.group(1) != null) {
        terms.add(matcher.group(1));
      } else {
        terms.add(matcher.group());
      }
    }

    return terms;
  }
}
