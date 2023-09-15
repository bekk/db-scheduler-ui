package com.github.bekk.exampleapp.service;

import static com.github.bekk.exampleapp.tasks.ChainTask.CHAINED_STEP_1_TASK;
import static com.github.bekk.exampleapp.tasks.FailingTask.FAILING_ONETIME_TASK;
import static com.github.bekk.exampleapp.tasks.LongRunningTask.LONG_RUNNING_ONETIME_TASK;
import static com.github.bekk.exampleapp.tasks.OneTimeTaskExample.ONE_TIME_TASK;

import com.github.bekk.exampleapp.model.TaskData;
import com.github.bekk.exampleapp.model.TestObject;
import com.github.kagkarlsson.scheduler.Scheduler;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
  private final Scheduler scheduler;

  public TaskService(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  public void runManuallyTriggeredTasks() {
    scheduler.schedule(ONE_TIME_TASK.instance("1", new TaskData(1, "test data")), Instant.now());

    scheduler.schedule(
        CHAINED_STEP_1_TASK.instance("3", new TestObject("Ole Nordman", 1, "ole.nordman@mail.com")),
        Instant.now());

    scheduler.schedule(LONG_RUNNING_ONETIME_TASK.instance("5"), Instant.now().plusSeconds(2));
    scheduler.schedule(FAILING_ONETIME_TASK.instance("6"), Instant.now().plusSeconds(2));
  }
}
