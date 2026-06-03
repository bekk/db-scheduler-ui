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

/**
 * One aggregated row on the Overview page — all instances of a single task name collapsed into a
 * single health summary.
 *
 * @param taskName the task name
 * @param recurring {@code true}/{@code false} when the registered task definitions are known,
 *     {@code null} in degraded mode where no task definitions are available
 * @param instanceCount number of scheduled executions for this task name ({@code 0} for dormant)
 * @param counts per-state instance counts
 * @param worstStatus the most severe state present
 * @param nextExecutionTime soonest next execution across instances, or {@code null}
 * @param lastSuccess most-recent last-success across instances, or {@code null}
 * @param lastFailure most-recent last-failure across instances, or {@code null}
 * @param maxConsecutiveFailures highest consecutive-failure count across instances
 */
public record OverviewTask(
    String taskName,
    Boolean recurring,
    int instanceCount,
    OverviewCounts counts,
    WorstStatus worstStatus,
    Instant nextExecutionTime,
    Instant lastSuccess,
    Instant lastFailure,
    int maxConsecutiveFailures) {}
