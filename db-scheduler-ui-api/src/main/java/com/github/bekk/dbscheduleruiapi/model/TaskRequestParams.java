package com.github.bekk.dbscheduleruiapi.model;

public class TaskRequestParams {
    
    private TaskFilter filter;
    private int pageNumber;
    private int size;

    public TaskRequestParams() {
    }

    public TaskRequestParams(TaskFilter filter, int pageNumber, int size) {
        System.out.println(size);
        this.filter = filter;
        this.pageNumber = pageNumber;
        this.size = size;
    }

    public TaskFilter getFilter() {
        return filter;
    }

    public void setFilter(TaskFilter filter) {
        this.filter = filter;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public enum TaskFilter {
        FAILED,
        RUNNING,
        SCHEDULED;
    }
}
