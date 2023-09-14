package com.github.bekk.dbscheduleruiapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GetTasksResponse {
  private final int numberOfTasks;
  private final int numberOfPages;
  private final List<TaskModel> tasks;

  @JsonCreator
  public GetTasksResponse(
      @JsonProperty("numberOfTasks") int totalTasks,
      @JsonProperty("tasks") List<TaskModel> pagedTasks,
      @JsonProperty("pageSize") int pageSize) {
    this.numberOfTasks = totalTasks;
    this.numberOfPages = totalTasks == 0 ? 0 : (int) Math.ceil((double) totalTasks / pageSize);
    this.tasks = pagedTasks;
  }

  public int getNumberOfTasks() {
    return numberOfTasks;
  }

  public int getNumberOfPages() {
    return numberOfPages;
  }

  public List<TaskModel> getTasks() {
    return tasks;
  }
}
