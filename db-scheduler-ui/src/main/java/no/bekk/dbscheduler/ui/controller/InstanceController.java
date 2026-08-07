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

import no.bekk.dbscheduler.ui.model.InstanceDetail;
import no.bekk.dbscheduler.ui.service.InstanceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin
@RequestMapping("/db-scheduler-api/tasks")
public class InstanceController {

  private final InstanceService instanceService;

  public InstanceController(InstanceService instanceService) {
    this.instanceService = instanceService;
  }

  /**
   * One execution in full.
   *
   * <p>Identity travels as query parameters, not path segments: instance ids are application-chosen
   * strings that routinely contain slashes, colons and spaces.
   *
   * @param id the instance; when omitted the task's sole execution is used, which is how the
   *     overview opens this — its rows name a task, not an instance. A task with several executions
   *     then answers 409: the request has not said which one it means.
   */
  @GetMapping("/instance")
  public InstanceDetail getInstance(
      @RequestParam String taskName, @RequestParam(required = false) String id) {
    if (id != null) {
      return instanceService
          .getInstance(taskName, id)
          .orElseThrow(() -> notFound("No execution '" + id + "' scheduled for task " + taskName));
    }

    InstanceService.SoleInstance sole = instanceService.getSoleInstance(taskName);
    if (sole.detail() != null) {
      return sole.detail();
    }
    if (sole.executionCount() == 0) {
      throw notFound("Task " + taskName + " has no scheduled executions");
    }
    throw new ResponseStatusException(
        HttpStatus.CONFLICT,
        "Task "
            + taskName
            + " has "
            + sole.executionCount()
            + " scheduled executions — pass id to choose one");
  }

  private static ResponseStatusException notFound(String message) {
    return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
  }
}
