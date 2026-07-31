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

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import java.time.Instant;
import no.bekk.dbscheduler.ui.service.TaskLogic;

@Controller("/db-scheduler-api/tasks")
@Requires(property = "db-scheduler-ui.read-only", notEquals = "true")
public class MicronautTaskAdminController {

  private final TaskLogic taskLogic;

  public MicronautTaskAdminController(TaskLogic taskLogic) {
    this.taskLogic = taskLogic;
  }

  @Post("/rerun")
  public void runNow(
      @QueryValue String id, @QueryValue String name, @QueryValue @Nullable Instant scheduleTime) {
    taskLogic.runTaskNow(id, name, scheduleTime);
  }

  @Post("/rerunGroup")
  public void runAllNow(@QueryValue String name, @QueryValue boolean onlyFailed) {
    taskLogic.runTaskGroupNow(name, onlyFailed);
  }

  @Post("/delete")
  public void deleteTaskNow(@QueryValue String id, @QueryValue String name) {
    taskLogic.deleteTask(id, name);
  }
}
