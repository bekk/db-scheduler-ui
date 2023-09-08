package com.github.bekk.dbscheduleruiapi.util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.bekk.dbscheduleruiapi.model.TaskModel;
import com.github.bekk.dbscheduleruiapi.model.TaskRequestParams.TaskFilter;
import com.github.bekk.dbscheduleruiapi.model.TaskRequestParams.TaskSort;

public class QueryUtils {

    public static <T> List<T> paginate(List<T> allItems, int pageNumber, int pageSize) {
        int startIndex = pageNumber * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allItems.size());

        return (startIndex < endIndex) ? allItems.subList(startIndex, endIndex) : new ArrayList<>();
    }

    public static List<TaskModel> filterTasks(List<TaskModel> tasks, TaskFilter filter) {
        return tasks.stream()
            .filter(task -> {
                switch (filter) {
                    case FAILED:
                        return task.getConsecutiveFailures().stream().anyMatch(failures -> failures != 0);
                    case RUNNING:
                        return task.isPicked().get(0);
                    case SCHEDULED:
                        return IntStream.range(0, task.isPicked().size())
                                .anyMatch(i -> !task.isPicked().get(i) && task.getConsecutiveFailures().get(i) == 0);
                    default:
                        return true;
                }
            })
            .collect(Collectors.toList());
    }


    public static List<TaskModel> sortTasks(List<TaskModel> tasks, TaskSort sortType, boolean isAsc) {
        if (sortType == TaskSort.NAME) {
            tasks.sort((task1, task2) -> {
                int comparisonResult = task1.getTaskName().compareTo(task2.getTaskName());
                return isAsc ? comparisonResult : -comparisonResult;
            });
        } else if (sortType == TaskSort.DEFAULT) {
            Comparator<TaskModel> comparator = Comparator.comparing(
                    task -> task.getExecutionTime().stream()
                            .min(Instant::compareTo)
                            .orElse(Instant.MAX),
                    Comparator.nullsLast(Instant::compareTo));
            tasks.sort(isAsc ? comparator : comparator.reversed());
        }
        return tasks;
    }
    
}
