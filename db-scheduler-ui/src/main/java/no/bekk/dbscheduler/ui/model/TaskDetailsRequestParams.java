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

public class TaskDetailsRequestParams extends TaskRequestParams {

  private final String taskId;
  private final String taskName;

  public TaskDetailsRequestParams(
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
      String taskName,
      String taskId,
      Boolean refresh) {
    super(filter, pageNumber, size, sorting, asc, searchTermTaskName, searchTermTaskInstance,taskNameExactMatch,taskInstanceExactMatch,startTime, endTime, refresh);
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
