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
 * Test-data builder for {@link TaskDetailsRequestParams}. Any field left unset falls through to the
 * constructor's default (e.g. {@code filter=ALL}, {@code pageNumber=0}, {@code asc=true}, {@code
 * refresh=true}).
 */
public final class TaskDetailsRequestParamsBuilder {

  private TaskFilter filter;
  private final Integer pageNumber = null;
  private Integer size;
  private final TaskSort sorting = null;
  private final Boolean asc = null;
  private final String searchTermTaskName = null;
  private final String searchTermTaskInstance = null;
  private final Boolean taskNameExactMatch = null;
  private final Boolean taskInstanceExactMatch = null;
  private final Instant startTime = null;
  private final Instant endTime = null;
  private final String taskName = null;
  private final String taskId = null;
  private final Boolean refresh = null;

  private TaskDetailsRequestParamsBuilder() {}

  public static TaskDetailsRequestParamsBuilder builder() {
    return new TaskDetailsRequestParamsBuilder();
  }

  public TaskDetailsRequestParamsBuilder filter(TaskFilter filter) {
    this.filter = filter;
    return this;
  }

  public TaskDetailsRequestParamsBuilder size(int size) {
    this.size = size;
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
