package com.github.bekk.dbscheduleruiapi.model;

import java.util.Optional;

public class TaskDetailsRequestParams extends TaskRequestParams {

    private String taskId;
    private String taskName;

    public TaskDetailsRequestParams() {
    }

    public TaskDetailsRequestParams(TaskFilter filter, int pageNumber, int size, TaskSort sorting, boolean asc, String taskName, String taskId) {
        super(filter, pageNumber, size, sorting, asc);
        this.taskId = taskId;
        this.taskName = taskName;
        System.out.println(taskName);
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}
