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
package no.bekk.dbscheduler.ui.model;

import java.time.Instant;
import lombok.Getter;

@Getter
public class TaskRequestParams {

  private final TaskFilter filter;
  private final int pageNumber;
  private final int size;
  private final TaskSort sorting;
  private final boolean asc;
  private final String searchTermTaskName;
  private final String searchTermTaskInstance;
  private final boolean taskNameExactMatch;
  private final boolean taskInstanceExactMatch;
  private final Instant startTime;
  private final Instant endTime;
  private final boolean refresh;

  public TaskRequestParams(
      TaskFilter filter,
      Integer pageNumber,
      Integer size,
      TaskSort sorting,
      Boolean asc,
      String searchTermTaskName,
      String searchTermTaskInstance,
      Boolean taskNameExactMatch,
      Boolean taskInstanceExactMatch,
      Instant startTime,
      Instant endTime,
      Boolean refresh) {
    this.filter = filter != null ? filter : TaskFilter.ALL;
    this.pageNumber = pageNumber != null ? pageNumber : 0;
    this.size = size != null ? size : 10;
    this.sorting = sorting != null ? sorting : TaskSort.DEFAULT;
    this.asc = asc != null ? asc : true;
    this.searchTermTaskName = searchTermTaskName;
    this.searchTermTaskInstance = searchTermTaskInstance;
    this.taskNameExactMatch = taskNameExactMatch != null ? taskNameExactMatch : false;
    this.taskInstanceExactMatch = taskInstanceExactMatch != null ? taskInstanceExactMatch : false;
    this.startTime = startTime;
    this.endTime = endTime;
    this.refresh = refresh != null ? refresh : true;
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
