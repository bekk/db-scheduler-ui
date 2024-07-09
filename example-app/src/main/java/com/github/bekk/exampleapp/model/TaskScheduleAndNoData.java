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
package com.github.bekk.exampleapp.model;

import com.github.kagkarlsson.scheduler.task.helper.ScheduleAndData;
import com.github.kagkarlsson.scheduler.task.schedule.CronSchedule;
import java.io.Serializable;

public class TaskScheduleAndNoData implements ScheduleAndData, Serializable {

  private static final long serialVersionUID = 1L; // recommended when using Java serialization

  private final CronSchedule schedule;

  private TaskScheduleAndNoData() {
    this(null);
  }

  public TaskScheduleAndNoData(CronSchedule schedule) {
    this.schedule = schedule;
  }

  @Override
  public CronSchedule getSchedule() {
    return this.schedule;
  }

  @Override
  public Object getData() {
    return null;
  }
}
