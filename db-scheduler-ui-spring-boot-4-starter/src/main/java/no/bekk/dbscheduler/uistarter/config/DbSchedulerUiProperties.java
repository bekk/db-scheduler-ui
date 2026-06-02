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
package no.bekk.dbscheduler.uistarter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration for the db-scheduler UI.
 *
 * @param enabled whether the UI auto-configuration is active
 * @param readOnly when true, disables mutating endpoints (rerun, delete, reschedule)
 * @param taskData whether to surface serialized task data in API responses
 * @param history whether the UI exposes task execution history (requires the log writer)
 * @param logLimit cap on the number of history rows fetched per request; 0 applies no explicit
 *     {@code LIMIT} clause, but the result set is still capped at 500 rows by the query layer
 */
@ConfigurationProperties("db-scheduler-ui")
public record DbSchedulerUiProperties(
    @DefaultValue("true") boolean enabled,
    @DefaultValue("false") boolean readOnly,
    @DefaultValue("true") boolean taskData,
    @DefaultValue("false") boolean history,
    @DefaultValue("0") int logLimit) {}
