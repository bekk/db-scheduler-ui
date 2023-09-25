package com.github.bekk.dbscheduleruiapi.controller;

import com.github.bekk.dbscheduleruiapi.model.*;
import com.github.bekk.dbscheduleruiapi.service.LogLogic;
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

  @GetMapping("/all")
  public GetLogsResponse getAllLogs(TaskDetailsRequestParams params) {
    return logLogic.getLogs(params);
  }

  @GetMapping("/poll")
  public LogPollResponse pollLogs(TaskDetailsRequestParams params) {
    return logLogic.pollLogs(params);
  }
}
