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

import static utils.Utils.sleep;

import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import java.util.Random;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecurringTaskExample {

  public static final TaskDescriptor<Void> RECURRING_TASK = TaskDescriptor.of("recurring-task");

  @Bean
  public RecurringTask<Void> getExample() {
    return Tasks.recurring(RECURRING_TASK, FixedDelay.ofSeconds(6))
        .execute(
            (inst, ctx) -> {
              sleep(5000);
              if (new Random().nextInt(100) < 30) {
                throw new RuntimeException("Simulated failure in example recurring task");
              }

              System.out.println("Executed recurring task: " + inst.getTaskName());
            });
  }
}
