package com.github.bekk.dbscheduleruiapi.model;


import java.util.List;


public class GetTasksResponse {
    private int numberOfTasks;
    private int numberOfPages;
    private List<TaskModel> tasks;

    public GetTasksResponse(int totalTasks, List<TaskModel> pagedTasks) {
        this.numberOfTasks = totalTasks;
        this.numberOfPages = (int) Math.ceil((double) totalTasks / pagedTasks.size());
        this.tasks = pagedTasks;
    }

    public int getNumberOfTasks() {
        return numberOfTasks;
    }

    public void setNumberOfTasks(int numberOfTasks) {
        this.numberOfTasks = numberOfTasks;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public List<TaskModel> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskModel> tasks) {
        this.tasks = tasks;
    }
}
