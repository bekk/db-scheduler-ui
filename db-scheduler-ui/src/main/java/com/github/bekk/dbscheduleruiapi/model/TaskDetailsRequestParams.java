package com.github.bekk.dbscheduleruiapi.model;

import java.time.Instant;

public class TaskDetailsRequestParams extends TaskRequestParams {

  private final String taskId;
  private final String taskName;

  public TaskDetailsRequestParams(
      TaskFilter filter,
      Integer pageNumber,
      Integer size,
      TaskSort sorting,
      Boolean asc,
      String searchTerm,
      Instant startTime,
      Instant endTime,
      String taskName,
      String taskId,
      Boolean refresh) {
    super(filter, pageNumber, size, sorting, asc, searchTerm, startTime, endTime, refresh);
    this.taskId = taskId;
    this.taskName = taskName;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getTaskName() {
    return taskName;
  }
}
