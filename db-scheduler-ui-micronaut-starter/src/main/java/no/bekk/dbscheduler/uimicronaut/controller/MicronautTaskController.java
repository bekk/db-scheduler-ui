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
package no.bekk.dbscheduler.uimicronaut.controller;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import java.time.Instant;
import no.bekk.dbscheduler.ui.model.GetTasksResponse;
import no.bekk.dbscheduler.ui.model.PollResponse;
import no.bekk.dbscheduler.ui.model.TaskDetailsRequestParams;
import no.bekk.dbscheduler.ui.model.TaskRequestParams;
import no.bekk.dbscheduler.ui.model.TaskRequestParams.TaskFilter;
import no.bekk.dbscheduler.ui.model.TaskRequestParams.TaskSort;
import no.bekk.dbscheduler.ui.service.TaskLogic;

@Controller("/db-scheduler-api/tasks")
public class MicronautTaskController {

  private final TaskLogic taskLogic;

  public MicronautTaskController(TaskLogic taskLogic) {
    this.taskLogic = taskLogic;
  }

  @Get("/all")
  public GetTasksResponse getTasks(
      @QueryValue @Nullable TaskFilter filter,
      @QueryValue @Nullable Integer pageNumber,
      @QueryValue @Nullable Integer size,
      @QueryValue @Nullable TaskSort sorting,
      @QueryValue @Nullable Boolean asc,
      @QueryValue @Nullable String searchTermTaskName,
      @QueryValue @Nullable String searchTermTaskInstance,
      @QueryValue @Nullable Boolean taskNameExactMatch,
      @QueryValue @Nullable Boolean taskInstanceExactMatch,
      @QueryValue @Nullable Instant startTime,
      @QueryValue @Nullable Instant endTime,
      @QueryValue @Nullable Boolean refresh) {
    return taskLogic.getAllTasks(
        new TaskRequestParams(
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
            refresh));
  }

  @Get("/details")
  public GetTasksResponse getTaskDetails(
      @QueryValue @Nullable TaskFilter filter,
      @QueryValue @Nullable Integer pageNumber,
      @QueryValue @Nullable Integer size,
      @QueryValue @Nullable TaskSort sorting,
      @QueryValue @Nullable Boolean asc,
      @QueryValue @Nullable String searchTermTaskName,
      @QueryValue @Nullable String searchTermTaskInstance,
      @QueryValue @Nullable Boolean taskNameExactMatch,
      @QueryValue @Nullable Boolean taskInstanceExactMatch,
      @QueryValue @Nullable Instant startTime,
      @QueryValue @Nullable Instant endTime,
      @QueryValue @Nullable String taskName,
      @QueryValue @Nullable String taskId,
      @QueryValue @Nullable Boolean refresh) {
    return taskLogic.getTask(
        new TaskDetailsRequestParams(
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
            refresh));
  }

  @Get("/poll")
  public PollResponse pollForUpdates(
      @QueryValue @Nullable TaskFilter filter,
      @QueryValue @Nullable Integer pageNumber,
      @QueryValue @Nullable Integer size,
      @QueryValue @Nullable TaskSort sorting,
      @QueryValue @Nullable Boolean asc,
      @QueryValue @Nullable String searchTermTaskName,
      @QueryValue @Nullable String searchTermTaskInstance,
      @QueryValue @Nullable Boolean taskNameExactMatch,
      @QueryValue @Nullable Boolean taskInstanceExactMatch,
      @QueryValue @Nullable Instant startTime,
      @QueryValue @Nullable Instant endTime,
      @QueryValue @Nullable String taskName,
      @QueryValue @Nullable String taskId,
      @QueryValue @Nullable Boolean refresh) {
    return taskLogic.pollTasks(
        new TaskDetailsRequestParams(
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
            refresh));
  }
}
