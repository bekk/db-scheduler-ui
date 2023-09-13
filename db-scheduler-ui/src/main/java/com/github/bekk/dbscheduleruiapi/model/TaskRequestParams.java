package com.github.bekk.dbscheduleruiapi.model;

public class TaskRequestParams {

  private final TaskFilter filter;
  private final int pageNumber;
  private final int size;
  private final TaskSort sorting;
  private final boolean asc;
  private final String searchTerm;

  public TaskRequestParams(
      TaskFilter filter,
      int pageNumber,
      int size,
      TaskSort sorting,
      boolean asc,
      String searchTerm) {
    this.filter = filter;
    this.pageNumber = pageNumber;
    this.size = size;
    this.sorting = sorting;
    this.asc = asc;
    this.searchTerm = searchTerm;
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

  public String getSearchTerm() {
    return searchTerm;
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
