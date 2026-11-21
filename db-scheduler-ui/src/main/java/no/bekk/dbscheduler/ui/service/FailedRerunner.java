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

import com.github.kagkarlsson.scheduler.ExecutionTimeAndId;
import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.ScheduledExecutionsFilter;
import com.github.kagkarlsson.scheduler.SchedulerClient;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Reruns a task's failing executions, batch by batch, without loading them all.
 *
 * <p>Only executions scheduled before the cutoff are touched. A rerun moves an execution to now,
 * which is not before the cutoff, so one call never reruns the same execution twice, even if it
 * fails again while the call is still running.
 */
public class FailedRerunner {

  public static final int DEFAULT_BATCH_SIZE = 500;

  private final SchedulerClient scheduler;
  private final Clock clock;
  private final int batchSize;

  public FailedRerunner(SchedulerClient scheduler, Clock clock, int batchSize) {
    this.scheduler = scheduler;
    this.clock = clock;
    this.batchSize = batchSize;
  }

  /**
   * @return how many executions were rescheduled; executions picked or removed in the meantime are
   *     skipped
   */
  public int rerunFailed(String taskName, Instant cutoff) {
    ExecutionTimeAndId before = new ExecutionTimeAndId(cutoff, "");
    // Both bounds keep the range ascending; db-scheduler sorts descending with only an upper one.
    ExecutionTimeAndId after = ExecutionPager.START;
    int rescheduled = 0;

    while (true) {
      List<ScheduledExecution<Object>> batch =
          scheduler.getScheduledExecutionsForTask(
              taskName,
              Object.class,
              ScheduledExecutionsFilter.all()
                  .withPicked(false)
                  .withFailing(true)
                  .after(after)
                  .before(before)
                  .limit(batchSize));
      for (ScheduledExecution<Object> execution : batch) {
        if (scheduler.tryReschedule(execution.getTaskInstance(), clock.instant())) {
          rescheduled++;
        }
      }
      if (batch.size() < batchSize) {
        return rescheduled;
      }
      after = ExecutionTimeAndId.from(batch.get(batch.size() - 1));
    }
  }
}
