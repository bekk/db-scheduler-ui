package com.github.bekk.dbscheduleruibackend.service;

import com.github.bekk.dbscheduleruibackend.example.*;
import com.github.bekk.dbscheduleruibackend.example.OneTimeTaskExample;
import com.github.bekk.dbscheduleruibackend.model.*;
import com.github.bekk.dbscheduleruibackend.util.mapper.TaskMapper;
import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.TaskInstanceId;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class TaskService {

    private final Scheduler scheduler;

    @Autowired
    public TaskService(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.scheduler.start();
    }

public void runTaskNow(String taskId, String taskName) {
    Optional<ScheduledExecution<Object>> scheduledExecutionOpt = scheduler.getScheduledExecution(TaskInstanceId.of(taskName, taskId));

    if (scheduledExecutionOpt.isPresent()) {
        TaskInstanceId taskInstance = scheduledExecutionOpt.get().getTaskInstance();
        scheduler.reschedule(taskInstance, Instant.now());
    } else {
        // Handle the case where the ScheduledExecution is not found
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No ScheduledExecution found for taskName: " + taskName + ", taskId: " + taskId);
    }
}

    public void deleteTask(String taskId, String taskName) {
        Optional<ScheduledExecution<Object>> scheduledExecutionOpt = scheduler.getScheduledExecution(TaskInstanceId.of(taskName, taskId));

        if (scheduledExecutionOpt.isPresent()) {
            TaskInstanceId taskInstance = scheduledExecutionOpt.get().getTaskInstance();
            scheduler.cancel(taskInstance);
        } else {
            // Handle the case where the ScheduledExecution is not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No ScheduledExecution found for taskName: " + taskName + ", taskId: " + taskId);
        }
    }

    public void runTaskByType(TaskType taskType) {
        switch (taskType) {
            case ONE_TIME -> runOneTimeTask();
            case RECURRING -> runRecurringTask();
            case CHAIN -> runChainTask();
            default -> throw new IllegalArgumentException("Task type not supported");
        }
    }

    public void runOneTimeTask() {
        long randomUUID = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
        scheduler.schedule(OneTimeTaskExample.getExample().instance(Long.toString(randomUUID), new TaskData(randomUUID, "test")), Instant.now().plusSeconds(100));
    }

    public void runRecurringTask() {
        scheduler.schedule(RecurringTaskExample.getExample().instance("1"), Instant.now().plusSeconds(1));
    }

    public void runChainTask() {
        long randomUUID = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
        System.out.println("Chain test");
        scheduler.schedule(ChainTask.chainTaskStepOne().instance(Long.toString(randomUUID), new TestObject("Ole Nordman", 3, "ole.nordman@mail.com")), Instant.now().plusSeconds(100));
    }

    public void runAllTasks(){
        for (TaskInstance<?> taskInstance : TaskDefinitions.getAllKnownTaskInstances()) {
            scheduler.schedule(taskInstance, Instant.now().plusSeconds(1));
        }
    }

    public GetTasksResponse getAllTasks(TaskRequestParams params) {
        List<TaskModel> tasks = TaskMapper.mapAllExecutionsToTaskModel(scheduler.getScheduledExecutions(), scheduler.getCurrentlyExecuting()).stream().filter(task -> {
            if (params.getFilter() != TaskRequestParams.TaskFilter.ALL) {
                return switch (params.getFilter()) {
                    case FAILED -> task.getConsecutiveFailures() != 0;
                    case RUNNING -> task.isPicked();
                    case SCHEDULED -> !task.isPicked() && task.getConsecutiveFailures() == 0;
                    default -> throw new IllegalArgumentException("Unexpected value: " + params.getFilter());
                };
            }
            return true;
        }).collect(Collectors.toList());
    
        if (params.getSorting() == TaskRequestParams.TaskSort.NAME) {
            tasks.sort((task1, task2) -> {
                int comparisonResult = task1.getTaskName().compareTo(task2.getTaskName());
                return params.isAsc() ? comparisonResult : -comparisonResult;
            });
        }else if(params.getSorting() == TaskRequestParams.TaskSort.DEFAULT){
            tasks.sort((task1, task2) -> {
                int comparisonResult = task1.getExecutionTime().compareTo(task2.getExecutionTime());
                return params.isAsc() ? comparisonResult : -comparisonResult;
            });
        }
    
        int totalTasks = tasks.size();
        int numberOfPages = (int) Math.ceil((double) totalTasks / params.getSize());
    
        int startIndex = params.getPageNumber() * params.getSize();
        int endIndex = Math.min(startIndex + params.getSize(), totalTasks);
    
        List<TaskModel> pagedTasks = (startIndex < endIndex) ? tasks.subList(startIndex, endIndex) : new ArrayList<>();
    
        return new GetTasksResponse(totalTasks, numberOfPages, pagedTasks);
    }
    
}
