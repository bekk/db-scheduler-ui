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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

public class LegacyLogPropertyEnvironmentPostProcessor implements EnvironmentPostProcessor {

  static final String LEGACY_ENABLED = "db-scheduler-log.enabled";
  static final String LEGACY_TABLE_NAME = "db-scheduler-log.table-name";

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    Boolean legacyEnabled = environment.getProperty(LEGACY_ENABLED, Boolean.class);
    String legacyTable = environment.getProperty(LEGACY_TABLE_NAME);
    if (legacyEnabled == null && legacyTable == null) {
      return;
    }
    StringBuilder message =
        new StringBuilder("The 'db-scheduler-log.*' property prefix is no longer supported. ")
            .append("Rename your configuration to 'db-scheduler-ui.log.*':");
    if (legacyEnabled != null) {
      message
          .append("\n  - 'db-scheduler-log.enabled=")
          .append(legacyEnabled)
          .append("' -> 'db-scheduler-ui.log.enabled=")
          .append(legacyEnabled)
          .append("'");
    }
    if (legacyTable != null) {
      message
          .append("\n  - 'db-scheduler-log.table-name=")
          .append(legacyTable)
          .append("' -> 'db-scheduler-ui.log.table-name=")
          .append(legacyTable)
          .append("'");
    }
    message
        .append("\nAlso remove the io.rocketbase.extension:db-scheduler-log-spring-boot-starter")
        .append(" dependency; db-scheduler-ui now provides this functionality natively.");
    throw new IllegalStateException(message.toString());
  }
}
