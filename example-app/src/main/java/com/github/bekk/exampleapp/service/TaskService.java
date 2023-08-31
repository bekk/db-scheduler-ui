package com.github.bekk.exampleapp.service;

import com.github.bekk.exampleapp.tasks.TaskDefinitions;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TaskService {
    private final Scheduler scheduler;

    public TaskService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void runAllTasks(){
        for (TaskInstance<?> taskInstance : TaskDefinitions.getAllKnownTaskInstances()) {
            scheduler.schedule(taskInstance, Instant.now().plusSeconds(1));
        }
    }
}
