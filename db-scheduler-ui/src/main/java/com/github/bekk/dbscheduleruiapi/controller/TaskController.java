package com.github.bekk.dbscheduleruiapi.controller;

import com.github.bekk.dbscheduleruiapi.model.GetTasksResponse;
import com.github.bekk.dbscheduleruiapi.model.TaskDetailsRequestParams;
import com.github.bekk.dbscheduleruiapi.service.TaskLogic;
import com.github.bekk.dbscheduleruiapi.model.TaskRequestParams;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class TaskController {
    private final TaskLogic taskLogic;

    @Autowired
    public TaskController(TaskLogic taskLogic) {
        this.taskLogic = taskLogic;
    }

    @RequestMapping(value= {"/db-scheduler-ui/**", "/db-scheduler-ui"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }

    @GetMapping("/tasks")
    public GetTasksResponse getTasks(TaskRequestParams params) {
        return taskLogic.getAllTasks(params);
    }

    @GetMapping("/tasks/details")
    public GetTasksResponse getTasks(TaskDetailsRequestParams params) {
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
