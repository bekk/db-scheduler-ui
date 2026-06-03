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
package no.bekk.dbscheduler.ui.service;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import no.bekk.dbscheduler.ui.model.OverviewTask;
import no.bekk.dbscheduler.ui.util.mapper.OverviewMapper;

/**
 * Produces the task-centric Overview: one row per task name. Aggregation is done DB-side by
 * db-scheduler ({@link SchedulerClient#getScheduledExecutionsSummaryByTask()}); the registered task
 * definitions are used only to overlay the {@code recurring} flag and dormant rows.
 *
 * <p>Gracefully degrades when no task definitions are available (e.g. the UI runs against a bare
 * {@code SchedulerClient}): the aggregation still yields every task with executions, with {@code
 * recurring = null} and no dormant rows.
 */
public class OverviewLogic {

  private final SchedulerClient schedulerClient;
  private final List<Task<?>> registeredTasks;

  public OverviewLogic(SchedulerClient schedulerClient, List<Task<?>> registeredTasks) {
    this.schedulerClient = schedulerClient;
    this.registeredTasks = registeredTasks == null ? List.of() : registeredTasks;
  }

  public List<OverviewTask> getOverview() {
    boolean taskDefinitionsKnown = !registeredTasks.isEmpty();

    Set<String> allTaskNames =
        registeredTasks.stream().map(Task::getName).collect(Collectors.toSet());
    Set<String> recurringTaskNames =
        registeredTasks.stream()
            .filter(task -> task instanceof RecurringTask<?>)
            .map(Task::getName)
            .collect(Collectors.toSet());

    return OverviewMapper.toOverview(
        schedulerClient.getScheduledExecutionsSummaryByTask(),
        recurringTaskNames,
        allTaskNames,
        taskDefinitionsKnown);
  }
}
