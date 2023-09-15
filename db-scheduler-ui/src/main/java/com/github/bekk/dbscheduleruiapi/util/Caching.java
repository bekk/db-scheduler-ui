package com.github.bekk.dbscheduleruiapi.util;

import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.ScheduledExecutionsFilter;
import com.github.kagkarlsson.scheduler.Scheduler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Caching {

    private final ConcurrentHashMap<String, List<ScheduledExecution<Object>>> taskCache = new ConcurrentHashMap<>();
    public List<ScheduledExecution<Object>> getExecutionsFromCacheOrDB(boolean isRefresh, Scheduler scheduler) {
        if (isRefresh || !taskCache.containsKey("executions")) {
            List<ScheduledExecution<Object>> executions = scheduler.getScheduledExecutions();
            executions.addAll(scheduler.getScheduledExecutions(ScheduledExecutionsFilter.all().withPicked(true)));

            taskCache.put("executions", executions);
            System.out.println("Getting from DB");
            return executions;
        }
        System.out.println("Getting from cache");

        return taskCache.get("executions");
    }
}
