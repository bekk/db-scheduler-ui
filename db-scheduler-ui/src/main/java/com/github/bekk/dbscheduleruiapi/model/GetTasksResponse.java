package com.github.bekk.dbscheduleruiapi.model;


import java.util.List;


public class GetTasksResponse {
    private int numberOfTasks;
    private int numberOfPages;
    private List<TaskModel> tasks;

    public GetTasksResponse(int totalTasks, List<TaskModel> pagedTasks, int pageSize) {
        this.numberOfTasks = totalTasks;
        this.numberOfPages = totalTasks==0 ? 0 : (int) Math.ceil((double) totalTasks / pageSize);
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
