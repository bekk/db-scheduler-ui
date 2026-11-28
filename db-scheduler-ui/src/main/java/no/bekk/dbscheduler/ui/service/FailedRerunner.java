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
 * <p>Only executions whose last failure is before the cutoff are touched. An execution this rerun
 * fails again gets a later last failure, so one call never reruns the same execution twice; nor
 * does it count one twice when the rerun moves it further along the walk.
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
  public int rerunFailed(String taskName, Instant requestedCutoff) {
    Instant rerunAt = clock.instant();
    // The cutoff comes from the client's clock; never let it reach past this server's now, or a
    // fast client clock would let one call rerun an execution that failed again meanwhile.
    Instant cutoff = requestedCutoff.isAfter(rerunAt) ? rerunAt : requestedCutoff;
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
                  .limit(batchSize));
      for (ScheduledExecution<Object> execution : batch) {
        boolean failedBeforeCutoff =
            execution.getLastFailure() != null && execution.getLastFailure().isBefore(cutoff);
        // Moved here by this call already, and reached again further along the walk.
        boolean alreadyRerun = execution.getExecutionTime().equals(rerunAt);
        if (failedBeforeCutoff
            && !alreadyRerun
            && scheduler.tryReschedule(execution.getTaskInstance(), rerunAt)) {
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
