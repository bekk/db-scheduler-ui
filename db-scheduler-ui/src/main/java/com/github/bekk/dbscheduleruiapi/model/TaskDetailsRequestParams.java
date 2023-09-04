package com.github.bekk.dbscheduleruiapi.model;

import java.util.Optional;

public class TaskDetailsRequestParams extends TaskRequestParams {
    
    private Optional<String> taskId;
    private String taskName;

    public TaskDetailsRequestParams() {
    }

    public TaskDetailsRequestParams(TaskFilter filter, int pageNumber, int size, TaskSort sorting, boolean asc, String taskName, Optional<String> taskId) {
        super(filter, pageNumber, size, sorting, asc);
        this.taskId = taskId;
        this.taskName = taskName;
    }

    public Optional<String> getTaskId() {
        return taskId;
    }

    public void setTaskId(Optional<String> taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}
