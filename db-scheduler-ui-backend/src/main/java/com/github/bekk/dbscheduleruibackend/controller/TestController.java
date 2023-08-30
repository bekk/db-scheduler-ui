package com.github.bekk.dbscheduleruibackend.controller;

import com.github.bekk.dbscheduleruibackend.model.GetTasksResponse;
import com.github.bekk.dbscheduleruibackend.model.TaskRequestParams;
import com.github.bekk.dbscheduleruibackend.model.TaskType;
import com.github.bekk.dbscheduleruibackend.service.TaskService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(methods = {RequestMethod.GET, RequestMethod.POST}, origins = "*")
public class TestController {

    private final TaskService taskService;

    public TestController(TaskService taskService){
        this.taskService = taskService;
    }

    @GetMapping("api/run/example-onetime")
    public void oneTimeTaskExample(){
        taskService.runOneTimeTask();
    }

    @GetMapping("api/run/example-recurring")
    public void recurringTaskExample(){
        taskService.runRecurringTask();
    }

    @GetMapping("api/run/example-chain")
    public void chainExample(){
        taskService.runChainTask();
    }

    @GetMapping("api/run")
    public void runTaskByType(@RequestParam TaskType taskType){
        taskService.runTaskByType(taskType);
    }

    @GetMapping("api/tasks")
    public GetTasksResponse getTasks(TaskRequestParams params) {
        return taskService.getAllTasks(params);
    }

    @PostMapping("api/rerun")
    public void runNow(@RequestParam String id, @RequestParam String name) {
        taskService.runTaskNow(id, name);
    }

    @PostMapping("api/delete")
    public void deleteTaskNow(@RequestParam String id, @RequestParam String name) {
        taskService.deleteTask(id, name);
    }

    @GetMapping("api/run/all")
    public void runAllTasks(){
        taskService.runAllTasks();
    }
}
