package com.github.bekk.dbscheduleruiapi.model;

public class TaskRequestParams {

  private final TaskFilter filter;
  private final int pageNumber;
  private final int size;
  private final TaskSort sorting;
  private final boolean asc;
  private boolean refresh;
  private final String searchTerm;

  public TaskRequestParams(
      TaskFilter filter,
      Integer pageNumber,
      Integer size,
      TaskSort sorting,
      Boolean asc,
      Boolean refresh,
      String searchTerm) {
    this.filter = filter != null ? filter : TaskFilter.ALL;
    this.pageNumber = pageNumber != null ? pageNumber : 0;
    this.size = size != null ? size : 10;
    this.sorting = sorting != null ? sorting : TaskSort.DEFAULT;
    this.asc = asc != null ? asc : true;
    this.refresh = refresh != null ? refresh : true;
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

  public boolean isRefresh() {
    return refresh;
  }

  public String getSearchTerm() {
    return searchTerm;
  }

  public void setRefresh(boolean refresh){
    this.refresh = refresh;
  }

  public enum TaskFilter {
    ALL,
    FAILED,
    RUNNING,
    SCHEDULED,
    SUCCEEDED;
  }

  public enum TaskSort {
    DEFAULT,
    NAME;
  }
}
