/*
 * Copyright (C) Bekk
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.bekk.dbscheduler.ui.testsupport;

import java.time.Instant;
import no.bekk.dbscheduler.ui.model.TaskDetailsRequestParams;
import no.bekk.dbscheduler.ui.model.TaskRequestParams.TaskFilter;
import no.bekk.dbscheduler.ui.model.TaskRequestParams.TaskSort;

/**
 * Test-data builder for {@link TaskDetailsRequestParams}. Any field left unset falls through to
 * the constructor's default (e.g. {@code filter=ALL}, {@code pageNumber=0}, {@code asc=true},
 * {@code refresh=true}).
 */
public final class TaskDetailsRequestParamsBuilder {

  private TaskFilter filter;
  private Integer pageNumber;
  private Integer size;
  private TaskSort sorting;
  private Boolean asc;
  private String searchTermTaskName;
  private String searchTermTaskInstance;
  private Boolean taskNameExactMatch;
  private Boolean taskInstanceExactMatch;
  private Instant startTime;
  private Instant endTime;
  private String taskName;
  private String taskId;
  private Boolean refresh;

  private TaskDetailsRequestParamsBuilder() {}

  public static TaskDetailsRequestParamsBuilder builder() {
    return new TaskDetailsRequestParamsBuilder();
  }

  public TaskDetailsRequestParamsBuilder filter(TaskFilter filter) {
    this.filter = filter;
    return this;
  }

  public TaskDetailsRequestParamsBuilder pageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
    return this;
  }

  public TaskDetailsRequestParamsBuilder size(int size) {
    this.size = size;
    return this;
  }

  public TaskDetailsRequestParamsBuilder sorting(TaskSort sorting) {
    this.sorting = sorting;
    return this;
  }

  public TaskDetailsRequestParamsBuilder asc(boolean asc) {
    this.asc = asc;
    return this;
  }

  public TaskDetailsRequestParamsBuilder searchTermTaskName(String searchTermTaskName) {
    this.searchTermTaskName = searchTermTaskName;
    return this;
  }

  public TaskDetailsRequestParamsBuilder searchTermTaskInstance(String searchTermTaskInstance) {
    this.searchTermTaskInstance = searchTermTaskInstance;
    return this;
  }

  public TaskDetailsRequestParamsBuilder taskNameExactMatch(boolean taskNameExactMatch) {
    this.taskNameExactMatch = taskNameExactMatch;
    return this;
  }

  public TaskDetailsRequestParamsBuilder taskInstanceExactMatch(boolean taskInstanceExactMatch) {
    this.taskInstanceExactMatch = taskInstanceExactMatch;
    return this;
  }

  public TaskDetailsRequestParamsBuilder startTime(Instant startTime) {
    this.startTime = startTime;
    return this;
  }

  public TaskDetailsRequestParamsBuilder endTime(Instant endTime) {
    this.endTime = endTime;
    return this;
  }

  public TaskDetailsRequestParamsBuilder taskName(String taskName) {
    this.taskName = taskName;
    return this;
  }

  public TaskDetailsRequestParamsBuilder taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }

  public TaskDetailsRequestParamsBuilder refresh(boolean refresh) {
    this.refresh = refresh;
    return this;
  }

  public TaskDetailsRequestParams build() {
    return new TaskDetailsRequestParams(
        filter,
        pageNumber,
        size,
        sorting,
        asc,
        searchTermTaskName,
        searchTermTaskInstance,
        taskNameExactMatch,
        taskInstanceExactMatch,
        startTime,
        endTime,
        taskName,
        taskId,
        refresh);
  }
}
