package com.github.bekk.dbscheduleruiapi.model;

public class TaskRequestParams {

  private final TaskFilter filter;
  private final int pageNumber;
  private final int size;
  private final TaskSort sorting;
  private final boolean asc;
  private final boolean refresh;

  public TaskRequestParams(
      TaskFilter filter, int pageNumber, int size, TaskSort sorting, boolean asc, boolean refresh) {
    this.filter = filter;
    this.pageNumber = pageNumber;
    this.size = size;
    this.sorting = sorting;
    this.asc = asc;
    this.refresh = refresh;
  }

  public TaskFilter getFilter() {
    return filter;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public int getSize() {
    return size;
  }

  public TaskSort getSorting() {
    return sorting;
  }

  public boolean isAsc() {
    return asc;
  }

  public boolean isRefresh() {
    return refresh;
  }

  public enum TaskFilter {
    ALL,
    FAILED,
    RUNNING,
    SCHEDULED;
  }

  public enum TaskSort {
    DEFAULT,
    NAME;
  }
}
