package com.github.bekk.dbscheduleruiapi.controller;

import com.github.bekk.dbscheduleruiapi.model.LogModel;
import com.github.bekk.dbscheduleruiapi.model.TaskDetailsRequestParams;
import com.github.bekk.dbscheduleruiapi.service.LogLogic;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
  public List<LogModel> getLogsById(TaskDetailsRequestParams params) {
    return logLogic.getLogsById(params);
  }

  @GetMapping("/all")
  public List<LogModel> getAllLogs(
      TaskDetailsRequestParams params) {
    return logLogic.getAllLogs(params);
  }
}
