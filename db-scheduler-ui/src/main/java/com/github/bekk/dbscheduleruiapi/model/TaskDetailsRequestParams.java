package com.github.bekk.dbscheduleruiapi.model;

public class TaskDetailsRequestParams extends TaskRequestParams {

  private final String taskId;
  private final String taskName;

  public TaskDetailsRequestParams(
      TaskFilter filter,
      int pageNumber,
      int size,
      TaskSort sorting,
      boolean asc,
      String searchTerm,
      String taskName,
      String taskId) {
    super(filter, pageNumber, size, sorting, asc, searchTerm);
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
