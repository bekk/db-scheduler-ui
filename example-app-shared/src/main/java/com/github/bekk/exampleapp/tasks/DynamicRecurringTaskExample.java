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
package com.github.bekk.exampleapp.tasks;

import com.github.bekk.exampleapp.model.TaskScheduleAndNoData;
import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTaskWithPersistentSchedule;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import utils.Utils;

@Configuration
public class DynamicRecurringTaskExample {

  public static final TaskDescriptor<TaskScheduleAndNoData> CUSTOMER_SCHEDULED_REPORT =
      TaskDescriptor.of("customer-scheduled-report", TaskScheduleAndNoData.class);

  @Bean
  public RecurringTaskWithPersistentSchedule<TaskScheduleAndNoData> customerScheduledReport() {
    return Tasks.recurringWithPersistentSchedule(CUSTOMER_SCHEDULED_REPORT)
        .execute(
            (inst, ctx) -> {
              Utils.sleep(500);
              System.out.println("Delivered scheduled report to customer: " + inst.getId());
            });
  }
}
