package com.github.bekk.dbscheduleruibackend.model;


import java.util.List;

public class GetTasksResponse {
    private int numberOfTasks;
    private int numberOfPages;
    private List<TaskModel> tasks;

    public GetTasksResponse(int numberOfTasks, int numberOfPages, List<TaskModel> tasks) {
        this.numberOfTasks = numberOfTasks;
        this.numberOfPages = numberOfPages;
        this.tasks = tasks;
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
