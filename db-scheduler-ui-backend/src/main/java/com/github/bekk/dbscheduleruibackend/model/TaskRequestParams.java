package com.github.bekk.dbscheduleruibackend.model;

public class TaskRequestParams {
    
    private TaskFilter filter;
    private int pageNumber;
    private int size;
    private TaskSort sorting;
    public TaskRequestParams() {
    }

    public TaskRequestParams(TaskFilter filter, int pageNumber, int size, TaskSort sorting) {
        System.out.println(size);
        this.filter = filter;
        this.pageNumber = pageNumber;
        this.size = size;
        this.sorting = sorting;
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

    public TaskSort getSorting() {
        return sorting;
    }

    public void setSorting(TaskSort sorting) {
        this.sorting = sorting;
    }

    public enum TaskFilter {
        FAILED,
        RUNNING,
        SCHEDULED;
    }
    public enum TaskSort {
        DEFAULT,
        NAME;
    }
}
