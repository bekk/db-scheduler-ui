package no.bekk.dbscheduler.ui.controller;

import no.bekk.dbscheduler.ui.model.LogModel;
import no.bekk.dbscheduler.ui.model.TaskDetailsRequestParams;
import no.bekk.dbscheduler.ui.service.LogLogic;
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
  public List<LogModel> getLogsById(TaskDetailsRequestParams params) {
    return logLogic.getLogsById(params);
  }

  @GetMapping("/all")
  public List<LogModel> getAllLogs(TaskDetailsRequestParams params) {
    return logLogic.getAllLogs(params);
  }
}
