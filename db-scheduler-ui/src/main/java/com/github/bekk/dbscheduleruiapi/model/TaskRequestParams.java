package com.github.bekk.dbscheduleruiapi.model;

import java.time.Instant;

public class TaskRequestParams {

  private final TaskFilter filter;
  private final int pageNumber;
  private final int size;
  private final TaskSort sorting;
  private final boolean asc;
  private final String searchTerm;
  private final Instant startTime;
  private final Instant endTime;

  public TaskRequestParams(
      TaskFilter filter,
      Integer pageNumber,
      Integer size,
      TaskSort sorting,
      Boolean asc,
      String searchTerm,
      Instant startTime,
      Instant endTime) {
    this.filter = filter;
    this.pageNumber = pageNumber != null ? pageNumber : 0;
    this.size = size != null ? size : 10;
    this.sorting = sorting;
    this.asc = asc != null ? asc : true;
    this.searchTerm = searchTerm;
    this.startTime = startTime;
    this.endTime = endTime;
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

  public Instant getStartTime() {
    return startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public enum TaskFilter {
    ALL,
    FAILED,
    RUNNING,
    SCHEDULED,
    SUCCEEDED
  }

  public enum TaskSort {
    DEFAULT,
    NAME
  }
}
