package com.github.bekk.dbscheduleruiapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GetTasksResponse {
  private final int numberOfItems;
  private final int numberOfPages;
  private final List<TaskModel> items;

  @JsonCreator
  public GetTasksResponse(
          @JsonProperty("numberOfItems") int totalTasks,
          @JsonProperty("items") List<TaskModel> pagedTasks,
          @JsonProperty("pageSize") int pageSize) {
    this.numberOfItems = totalTasks;
    this.numberOfPages = totalTasks == 0 ? 0 : (int) Math.ceil((double) totalTasks / pageSize);
    this.items = pagedTasks;
  }

  public int getNumberOfItems() {
    return numberOfItems;
  }

  public int getNumberOfPages() {
    return numberOfPages;
  }

  public List<TaskModel> getItems() {
    return items;
  }
}