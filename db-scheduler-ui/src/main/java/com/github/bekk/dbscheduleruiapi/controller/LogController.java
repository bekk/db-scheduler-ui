package com.github.bekk.dbscheduleruiapi.controller;

import com.github.bekk.dbscheduleruiapi.model.LogModel;
import com.github.bekk.dbscheduleruiapi.service.LogLogic;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/db-scheduler-api/logs")
public class LogController {

  private final LogLogic logLogic;

  @Autowired
  public LogController(LogLogic logLogic) {
    this.logLogic = logLogic;
  }

  @GetMapping("/id")
  public List<LogModel> getLogsById(
      @RequestParam String taskName, @RequestParam String taskInstance) {
    return logLogic.getLogsById(taskName, taskInstance);
  }

  @GetMapping("/all")
  public List<LogModel> getAllLogs() {
    return logLogic.getAllLogs();
  }
}
