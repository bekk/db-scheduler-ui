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

import no.bekk.dbscheduler.ui.model.GetTasksResponse;
import no.bekk.dbscheduler.ui.model.PollResponse;
import no.bekk.dbscheduler.ui.model.TaskDetailsRequestParams;
import no.bekk.dbscheduler.ui.model.TaskRequestParams;
import no.bekk.dbscheduler.ui.service.TaskLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/db-scheduler-api/tasks")
public class TaskController {
  private final TaskLogic taskLogic;

  @Autowired
  public TaskController(TaskLogic taskLogic) {
    this.taskLogic = taskLogic;
  }

  @GetMapping("/all")
  public GetTasksResponse getTasks(TaskRequestParams params) {
    return taskLogic.getAllTasks(params);
  }

  @GetMapping("/details")
  public GetTasksResponse getTaskDetails(TaskDetailsRequestParams params) {
    return taskLogic.getTask(params);
  }

  @GetMapping("/poll")
  public PollResponse pollForUpdates(TaskDetailsRequestParams params) {
    return taskLogic.pollTasks(params);
  }

  @PostMapping("/rerun")
  public void runNow(@RequestParam String id, @RequestParam String name) {
    taskLogic.runTaskNow(id, name);
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
