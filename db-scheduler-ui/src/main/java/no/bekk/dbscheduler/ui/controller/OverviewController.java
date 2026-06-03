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
package no.bekk.dbscheduler.ui.controller;

import java.util.List;
import no.bekk.dbscheduler.ui.model.OverviewTask;
import no.bekk.dbscheduler.ui.service.OverviewLogic;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only, navigation-only endpoint backing the task-centric Overview page. */
@RestController
@CrossOrigin
@RequestMapping("/db-scheduler-api/tasks/overview")
public class OverviewController {

  private final OverviewLogic overviewLogic;

  public OverviewController(OverviewLogic overviewLogic) {
    this.overviewLogic = overviewLogic;
  }

  @GetMapping
  public List<OverviewTask> getOverview() {
    return overviewLogic.getOverview();
  }
}
