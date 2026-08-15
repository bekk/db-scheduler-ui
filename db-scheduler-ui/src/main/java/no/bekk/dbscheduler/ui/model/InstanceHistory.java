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
import java.util.List;

/**
 * What the log table knows about one instance.
 *
 * @param lastFailure the most recent failed run with its stack trace, or {@code null} if none of
 *     the fetched runs failed
 * @param recentRuns most recent first
 */
public record InstanceHistory(InstanceRun lastFailure, List<InstanceRun> recentRuns) {

  /**
   * @param stackTrace only populated on the {@code lastFailure} run; the list shows outcome and
   *     message only
   */
  public record InstanceRun(
      long id,
      boolean succeeded,
      Instant timeStarted,
      long durationMs,
      String exceptionClass,
      String exceptionMessage,
      String stackTrace) {}
}
