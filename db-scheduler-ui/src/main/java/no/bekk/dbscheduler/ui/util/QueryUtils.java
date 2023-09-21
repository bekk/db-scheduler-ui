package no.bekk.dbscheduler.ui.util;

import no.bekk.dbscheduler.ui.model.TaskModel;
import no.bekk.dbscheduler.ui.model.TaskRequestParams;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class QueryUtils {

  public static <T> List<T> paginate(List<T> allItems, int pageNumber, int pageSize) {
    int startIndex = pageNumber * pageSize;
    int endIndex = Math.min(startIndex + pageSize, allItems.size());

    return (startIndex < endIndex) ? allItems.subList(startIndex, endIndex) : new ArrayList<>();
  }

  public static List<TaskModel> filterTasks(List<TaskModel> tasks, TaskRequestParams.TaskFilter filter) {
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

  public static List<TaskModel> sortTasks(List<TaskModel> tasks, TaskRequestParams.TaskSort sortType, boolean isAsc) {
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

  public static List<TaskModel> search(List<TaskModel> tasks, String searchTerm) {
    if (searchTerm == null || searchTerm.trim().isEmpty()) {
      return tasks;
    }

    String lowerCaseTerm = searchTerm.toLowerCase();

    return tasks.stream()
        .filter(
            task ->
                task.getTaskName().toLowerCase().contains(lowerCaseTerm)
                    || task.getTaskInstance().stream()
                        .anyMatch(
                            instance ->
                                instance != null && instance.toLowerCase().contains(lowerCaseTerm)))
        .collect(Collectors.toList());
  }

  public static void logSearchCondition(
      Map<String, Object> params, String searchTerm, List<String> conditions) {
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
      conditions.add(String.join(" AND ", termConditions));
    }
  }

  public static void logFilterCondition(
      TaskRequestParams.TaskFilter filter, List<String> conditions) {
    if (filter != null && filter != TaskRequestParams.TaskFilter.ALL) {
      String filterCondition =
          filter == TaskRequestParams.TaskFilter.SUCCEEDED
              ? "succeeded = TRUE"
              : "succeeded = FALSE";
      conditions.add(filterCondition);
    }
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
