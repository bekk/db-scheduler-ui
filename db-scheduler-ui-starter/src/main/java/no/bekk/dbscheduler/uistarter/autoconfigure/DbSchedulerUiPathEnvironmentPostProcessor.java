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

import static no.bekk.dbscheduler.uistarter.config.DbSchedulerUiUtil.normalizePath;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class DbSchedulerUiPathEnvironmentPostProcessor implements EnvironmentPostProcessor {

  private static final String PROPERTY_SOURCE_NAME = "dbSchedulerUiNormalizedPaths";
  private static final String UI_PATH = "db-scheduler-ui.ui-path";
  private static final String API_PATH = "db-scheduler-ui.api-path";

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    Map<String, Object> normalizedPaths = new HashMap<>();
    normalize(environment, normalizedPaths, UI_PATH);
    normalize(environment, normalizedPaths, API_PATH);

    if (!normalizedPaths.isEmpty()) {
      environment
          .getPropertySources()
          .addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, normalizedPaths));
    }
  }

  private static void normalize(
      ConfigurableEnvironment environment, Map<String, Object> normalizedPaths, String property) {
    if (!environment.containsProperty(property)) {
      return;
    }

    String value = environment.getProperty(property);
    String normalized = normalizePath(value);
    if (!normalized.equals(value)) {
      normalizedPaths.put(property, normalized);
    }
  }
}
