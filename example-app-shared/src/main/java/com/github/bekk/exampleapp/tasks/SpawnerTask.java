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
import com.github.kagkarlsson.scheduler.task.schedule.Schedules;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Random;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpawnerTask {

  public static final TaskDescriptor<Void> ENQUEUE_DAILY_NEWSLETTER_BATCH =
      TaskDescriptor.of("enqueue-daily-newsletter-batch");

  public static final TaskDescriptor<TaskData> SEND_NEWSLETTER_EMAIL =
      TaskDescriptor.of("send-newsletter-email", TaskData.class);

  @Bean
  public Task<?> enqueueDailyNewsletterBatch() {
    return Tasks.recurring(ENQUEUE_DAILY_NEWSLETTER_BATCH, Schedules.cron("0 0 9 * * *"))
        .execute(
            (inst, ctx) -> {
              final SchedulerClient client = ctx.getSchedulerClient();
              final String today = LocalDate.now().toString();
              final Random random = new Random();

              for (int userId = 1; userId <= 30; userId++) {
                int spreadSeconds = random.nextInt(600);
                client.scheduleIfNotExists(
                    SEND_NEWSLETTER_EMAIL
                        .instance(today + "-user-" + userId)
                        .data(new TaskData(userId, "{newsletter: weekly-digest}", Instant.now()))
                        .build(),
                    Instant.now().plusSeconds(spreadSeconds));
              }
            });
  }

  @Bean
  public Task<TaskData> sendNewsletterEmail() {
    return Tasks.oneTime(SEND_NEWSLETTER_EMAIL)
        .execute(
            (inst, ctx) -> {
              sleep(2000);
              if (new Random().nextInt(100) < 20) {
                throw new RuntimeException("SMTP relay rejected message");
              }
            });
  }
}
