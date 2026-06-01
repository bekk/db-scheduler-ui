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
package no.bekk.dbscheduler.ui.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One row of the Overview page: all instances of a single task name aggregated into one object. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverviewTask {

  private String taskName;

  /**
   * {@code true} iff the registered definition is {@code instanceof RecurringTask} (a fixed,
   * self-rescheduling singleton). {@code RecurringTaskWithPersistentSchedule}, one-time and custom
   * tasks are {@code false}. {@code null} = could not determine (degraded mode: no task definitions
   * available).
   */
  private Boolean recurring;

  private int instanceCount;

  private OverviewCounts counts;

  private WorstStatus worstStatus;

  /** Soonest execution time across non-picked instances; {@code null} if none (all picked / 0). */
  private Instant nextExecutionTime;

  /**
   * Soonest firing time across picked (running) instances; powers the {@code running for} sub-line.
   */
  private Instant runningSince;

  /** Most-recent last-success across instances. */
  private Instant lastSuccess;

  /** Most-recent last-failure across instances. */
  private Instant lastFailure;

  private int maxConsecutiveFailures;
}
