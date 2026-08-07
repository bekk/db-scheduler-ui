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
 * One scheduled execution, flat.
 *
 * <p>Deliberately not {@link TaskModel}: that one is group-shaped (parallel lists plus task-level
 * scalars), so reading a single instance out of it is only correct by accident.
 *
 * <p>{@code version} and {@code lastHeartbeat} are absent because db-scheduler's {@code
 * ScheduledExecution} exposes neither — a field that can only ever be a placeholder is worse than
 * no field.
 *
 * @param history run log for this instance, or {@code null} when {@code db-scheduler-ui.history} is
 *     off — the exception and past runs live in the log table and nowhere else.
 */
public record InstanceDetail(
    String taskName,
    String id,
    InstanceStatus status,
    Instant executionTime,
    boolean picked,
    String pickedBy,
    Instant lastSuccess,
    Instant lastFailure,
    int consecutiveFailures,
    Object taskData,
    InstanceHistory history) {}
