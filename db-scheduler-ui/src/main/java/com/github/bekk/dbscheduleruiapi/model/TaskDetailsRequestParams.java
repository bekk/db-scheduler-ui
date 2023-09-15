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
      boolean refresh,
      String taskName,
      String taskId) {
    super(filter, pageNumber, size, sorting, asc, refresh);
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
