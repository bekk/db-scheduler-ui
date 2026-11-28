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
package no.bekk.dbscheduler.uistarter.autoconfigure;

import com.github.kagkarlsson.scheduler.boot.config.DbSchedulerCustomizer;
import com.github.kagkarlsson.scheduler.boot.config.DbSchedulerOverrides;
import com.github.kagkarlsson.scheduler.serializer.Serializer;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * The data source and serializer the application configured for db-scheduler, if any, so the UI
 * reads the same database and payloads as the scheduler.
 *
 * <p>Follows db-scheduler's own precedence: a {@link DbSchedulerOverrides} bean wins outright,
 * otherwise the deprecated {@link DbSchedulerCustomizer}. Neither bean is required. Deprecation
 * warnings are left to db-scheduler's starter, which already logs them.
 */
public record SchedulerSettings(Optional<DataSource> dataSource, Optional<Serializer> serializer) {

  @SuppressWarnings("removal")
  public static SchedulerSettings resolve(
      DbSchedulerOverrides overrides, DbSchedulerCustomizer customizer) {
    if (overrides != null) {
      return new SchedulerSettings(overrides.dataSource(), overrides.serializer());
    }
    if (customizer != null) {
      return new SchedulerSettings(customizer.dataSource(), customizer.serializer());
    }
    return new SchedulerSettings(Optional.empty(), Optional.empty());
  }
}
