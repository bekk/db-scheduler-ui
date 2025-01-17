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

import com.github.bekk.exampleapp.model.TaskData;
import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpawnerTask {

  public static final TaskDescriptor<Void> RECURRING_SPAWNER_TASK =
      TaskDescriptor.of("recurring-spawner-task");

  public static final TaskDescriptor<TaskData> ONE_TIME_SPAWNER_TASK =
      TaskDescriptor.of("onetime-spawned-task", TaskData.class);

  @Bean
  public Task<?> runSpawner() {
    return Tasks.recurring(RECURRING_SPAWNER_TASK, FixedDelay.ofSeconds(180))
        .execute(
            (inst, ctx) -> {
              final SchedulerClient client = ctx.getSchedulerClient();
              final long randomUUID = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;

              for (int i = 0; i < 100; i++) {
                client.scheduleIfNotExists(
                    ONE_TIME_SPAWNER_TASK
                        .instance("spawned " + randomUUID + " loopnr: " + i)
                        .data(new TaskData(123, "{data: MASSIVEDATA}", Instant.now()))
                        .build(),
                    Instant.now().plusSeconds(60));
              }
            });
  }

  @Bean
  public Task<TaskData> runOneTimeSpawned() {
    return Tasks.oneTime(ONE_TIME_SPAWNER_TASK)
        .execute(
            (inst, ctx) -> {
              sleep(10000);
              if (new Random().nextInt(100) < 20) {
                throw new RuntimeException("Simulated failure");
              }
            });
  }
}
