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

import no.bekk.dbscheduler.ui.model.LogModel;
import no.bekk.dbscheduler.ui.model.TaskDetailsRequestParams;
import no.bekk.dbscheduler.ui.service.LogLogic;
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
