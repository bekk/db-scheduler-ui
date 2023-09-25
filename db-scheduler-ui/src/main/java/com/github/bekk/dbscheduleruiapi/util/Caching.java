package com.github.bekk.dbscheduleruiapi.util;

import com.github.bekk.dbscheduleruiapi.model.LogModel;
import com.github.bekk.dbscheduleruiapi.model.TaskDetailsRequestParams;
import com.github.bekk.dbscheduleruiapi.service.LogLogic;
import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.ScheduledExecutionsFilter;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.TaskInstanceId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class Caching {

  private final Map<String, String> taskStatusCache = new ConcurrentHashMap<>();
  private final List<ScheduledExecution<Object>> taskDataCache = new ArrayList<>();
  private final Map<Long, LogModel> logCache = new ConcurrentHashMap<>();

  public List<ScheduledExecution<Object>> getExecutionsFromCacheOrDB(
      boolean isRefresh, Scheduler scheduler) {
    if (isRefresh || !taskDataCache.isEmpty()) {
      List<ScheduledExecution<Object>> executions =
          getExecutionsFromDBWithoutUpdatingCache(scheduler);
      updateCache(executions);
      return executions;
    } else {
      return taskDataCache;
    }
  }

  public List<ScheduledExecution<Object>> getExecutionsFromDBWithoutUpdatingCache(
      Scheduler scheduler) {
    List<ScheduledExecution<Object>> executions = scheduler.getScheduledExecutions();
    executions.addAll(
        scheduler.getScheduledExecutions(ScheduledExecutionsFilter.all().withPicked(true)));
    return executions;
  }

  public void updateCache(List<ScheduledExecution<Object>> executions) {
    taskStatusCache.clear();
    taskDataCache.clear();
    taskDataCache.addAll(executions);
    for (ScheduledExecution<Object> execution : executions) {
      taskStatusCache.put(getUniqueId(execution), getStatus(execution));
    }
  }

  public String getUniqueId(ScheduledExecution<Object> task) {
    return task.getTaskInstance().getTaskName() + "_" + task.getTaskInstance().getId();
  }

  public String getStatus(ScheduledExecution<Object> task) {
    return (task.getConsecutiveFailures() > 0 ? "1" : "0")
        + (task.getPickedBy() != null ? "1" : "0");
  }

  public String getStatusFromCache(TaskInstanceId taskInstance) {
    String uniqueId = taskInstance.getTaskName() + "_" + taskInstance.getId();
    return taskStatusCache.get(uniqueId);
  }

  public List<LogModel> getLogsFromCacheOrDB(
      boolean isRefresh, LogLogic logLogic, TaskDetailsRequestParams requestParams) {
    if (isRefresh || logCache.isEmpty()) {
      List<LogModel> logs = logLogic.getLogsDirectlyFromDB(requestParams);
      updateLogCache(logs);
      return logs;
    } else {
      return new ArrayList<>(logCache.values());
    }
  }

  public void updateLogCache(List<LogModel> logs) {
    logCache.clear();
    for (LogModel log : logs) {
      logCache.put(log.getId(), log);
    }
  }

  public boolean checkLogCacheForKey(Long key) {
    return logCache.containsKey(key);
  }
}
