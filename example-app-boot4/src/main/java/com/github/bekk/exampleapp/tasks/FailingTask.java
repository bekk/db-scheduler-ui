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

import static java.time.Duration.ofSeconds;

import com.github.kagkarlsson.scheduler.task.FailureHandler;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FailingTask {

  public static final TaskDescriptor<Void> FAILING_ONETIME_TASK =
      TaskDescriptor.of("failing-one-time-task");

  public static final TaskDescriptor<Void> FAILING_RECURRING_TASK =
      TaskDescriptor.of("failing-recurring-task");

  public static final TaskDescriptor<Void> FAILING_ONETIME_TASK_BACKOFF =
      TaskDescriptor.of("failing-one-time-with-backoff-task");

  @Bean
  public Task<?> runOneTimeFailing() {
    return Tasks.oneTime(FAILING_ONETIME_TASK)
        .execute(
            (inst, ctx) -> {
              throw new RuntimeException("Simulated task failure");
            });
  }

  @Bean
  public Task<?> runRecurringFailing() {
    return Tasks.recurring(FAILING_RECURRING_TASK, FixedDelay.ofSeconds(6))
        .execute(
            (inst, ctx) -> {
              throw new RuntimeException("Simulated task failure");
            });
  }

  @Bean
  public Task<?> runOneTimeFailingWithBackoff() {
    return Tasks.oneTime(FAILING_ONETIME_TASK_BACKOFF)
        .onFailure(new FailureHandler.ExponentialBackoffFailureHandler<>(ofSeconds(1)))
        .execute(
            (inst, ctx) -> {
              throw new RuntimeException("Simulated task failure");
            });
  }
}
