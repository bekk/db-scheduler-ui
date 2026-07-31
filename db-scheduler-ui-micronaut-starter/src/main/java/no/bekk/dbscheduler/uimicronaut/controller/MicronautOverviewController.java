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
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import java.util.List;
import no.bekk.dbscheduler.ui.model.OverviewTask;
import no.bekk.dbscheduler.ui.service.OverviewService;

@Controller("/db-scheduler-api/tasks")
@Requires(property = "db-scheduler-ui.overview", value = "true")
public class MicronautOverviewController {

  private final OverviewService overviewService;

  public MicronautOverviewController(OverviewService overviewService) {
    this.overviewService = overviewService;
  }

  @Get("/overview")
  public List<OverviewTask> getOverviewTasks() {
    return overviewService.getOverviewTasks();
  }
}
