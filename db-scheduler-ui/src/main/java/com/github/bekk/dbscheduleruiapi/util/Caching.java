package com.github.bekk.dbscheduleruiapi.util;

import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.ScheduledExecutionsFilter;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.TaskInstanceId;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Caching {

  private final Set<String> taskIdsCache = ConcurrentHashMap.newKeySet();
  private final List<ScheduledExecution<Object>> taskDataCache = new ArrayList<>();

  public List<ScheduledExecution<Object>> getExecutionsFromCacheOrDB(boolean isRefresh, Scheduler scheduler) {
    if (isRefresh || !taskDataCache.isEmpty()) {
      List<ScheduledExecution<Object>> executions = getExecutionsFromDBWithoutUpdatingCache(scheduler);
      updateCache(executions);
      System.out.println("Getting from DB");
      return executions;
    } else {
      System.out.println("Getting from cache");
      return taskDataCache;
    }
  }

  public List<ScheduledExecution<Object>> getExecutionsFromDBWithoutUpdatingCache(Scheduler scheduler) {
    List<ScheduledExecution<Object>> executions = scheduler.getScheduledExecutions();
    executions.addAll(scheduler.getScheduledExecutions(ScheduledExecutionsFilter.all().withPicked(true)));
    System.out.println("Checking DB");
    return executions;
  }

  public void updateCache(List<ScheduledExecution<Object>> executions) {
    taskIdsCache.clear();
    taskDataCache.clear();
    taskDataCache.addAll(executions);
    for (ScheduledExecution<Object> execution : executions) {
      String status = (execution.getConsecutiveFailures()>0?"1":"0")+(execution.getPickedBy().isEmpty() ? "1" : "0");
      String uniqueId = execution.getTaskInstance().getTaskName() + "_" + execution.getTaskInstance().getId() + "_" + status;
      taskIdsCache.add(uniqueId);
    }
  }

  public boolean isTaskInCache(TaskInstanceId taskInstance, String status) {
    String uniqueId = taskInstance.getTaskName() + "_" + taskInstance.getId() + "_" + status;
    return taskIdsCache.contains(uniqueId);
  }

  public List<ScheduledExecution<Object>> getTaskDataCache() {
    return taskDataCache;
  }
}
