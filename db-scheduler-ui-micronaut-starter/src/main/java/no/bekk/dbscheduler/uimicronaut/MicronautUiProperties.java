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
package no.bekk.dbscheduler.uimicronaut;

import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * Micronaut counterpart of the Spring starter's {@code DbSchedulerUiProperties}, bound from the
 * {@code db-scheduler-ui.*} configuration namespace. History and log properties are intentionally
 * omitted: the Micronaut integration does not wire the execution-log path.
 */
@ConfigurationProperties("db-scheduler-ui")
public class MicronautUiProperties {

  private boolean enabled = true;
  private boolean readOnly = false;
  private boolean taskData = true;
  private boolean history = false;
  private boolean overview = false;
  private String contextPath = "";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public boolean isTaskData() {
    return taskData;
  }

  public void setTaskData(boolean taskData) {
    this.taskData = taskData;
  }

  public boolean isHistory() {
    return history;
  }

  public void setHistory(boolean history) {
    this.history = history;
  }

  public boolean isOverview() {
    return overview;
  }

  public void setOverview(boolean overview) {
    this.overview = overview;
  }

  public String getContextPath() {
    return contextPath;
  }

  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }
}
