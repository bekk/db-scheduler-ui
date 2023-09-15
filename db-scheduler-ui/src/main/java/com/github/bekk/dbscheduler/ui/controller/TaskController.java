package com.github.bekk.dbscheduler.ui.controller;

import com.github.bekk.dbscheduler.ui.model.GetTasksResponse;
import com.github.bekk.dbscheduler.ui.model.TaskDetailsRequestParams;
import com.github.bekk.dbscheduler.ui.model.TaskRequestParams;
import com.github.bekk.dbscheduler.ui.service.TaskLogic;
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

  @PostMapping("/rerun")
  public void runNow(@RequestParam String id, @RequestParam String name) {
    taskLogic.runTaskNow(id, name);
  }

  @PostMapping("/delete")
  public void deleteTaskNow(@RequestParam String id, @RequestParam String name) {
    taskLogic.deleteTask(id, name);
  }
}
