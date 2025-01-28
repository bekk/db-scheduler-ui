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

import java.time.Instant;
import no.bekk.dbscheduler.ui.service.TaskLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/db-scheduler-api/tasks")
@ConditionalOnProperty(
    prefix = "db-scheduler-ui",
    name = "read-only",
    havingValue = "false",
    matchIfMissing = true)
public class TaskAdminController {

  private final TaskLogic taskLogic;

  @Autowired
  public TaskAdminController(TaskLogic taskLogic) {
    this.taskLogic = taskLogic;
  }

  @PostMapping("/rerun")
  public void runNow(
      @RequestParam String id, @RequestParam String name, @RequestParam Instant scheduleTime) {
    taskLogic.runTaskNow(id, name, scheduleTime);
  }

  @PostMapping("/rerunGroup")
  public void runAllNow(@RequestParam String name, @RequestParam boolean onlyFailed) {
    taskLogic.runTaskGroupNow(name, onlyFailed);
  }

  @PostMapping("/delete")
  public void deleteTaskNow(@RequestParam String id, @RequestParam String name) {
    taskLogic.deleteTask(id, name);
  }
}
