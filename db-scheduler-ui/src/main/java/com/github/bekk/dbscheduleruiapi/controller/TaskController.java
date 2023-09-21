package com.github.bekk.dbscheduleruiapi.controller;

import com.github.bekk.dbscheduleruiapi.model.GetTasksResponse;
import com.github.bekk.dbscheduleruiapi.model.PollResponse;
import com.github.bekk.dbscheduleruiapi.model.TaskDetailsRequestParams;
import com.github.bekk.dbscheduleruiapi.model.TaskRequestParams;
import com.github.bekk.dbscheduleruiapi.service.TaskLogic;
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

  @PostMapping("/delete")
  public void deleteTaskNow(@RequestParam String id, @RequestParam String name) {
    taskLogic.deleteTask(id, name);
  }
}
