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
    scheduler.schedule(
        ONE_TIME_TASK.instance("1").data(new TaskData(1, "test data", Instant.now())).build(),
        Instant.now());

    scheduler.schedule(
        CHAINED_STEP_1_TASK.instance("3").data(new TestObject("Ole Nordman", 1, "ole.nordman@mail.com")).build(),
        Instant.now());

    scheduler.schedule(LONG_RUNNING_ONETIME_TASK.instance("5").build(), Instant.now().plusSeconds(2));
    scheduler.schedule(FAILING_ONETIME_TASK.instance("6").build(), Instant.now().plusSeconds(2));
  }
}
