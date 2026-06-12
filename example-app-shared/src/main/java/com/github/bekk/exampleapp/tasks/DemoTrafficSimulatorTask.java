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

import static com.github.bekk.exampleapp.tasks.ChainTask.ORDER_CAPTURE_PAYMENT;
import static com.github.bekk.exampleapp.tasks.FailingTask.CHARGE_CREDIT_CARD;
import static com.github.bekk.exampleapp.tasks.FailingTask.DELIVER_OUTGOING_WEBHOOK;
import static com.github.bekk.exampleapp.tasks.LongRunningTask.GENERATE_MONTHLY_INVOICE_PDF;
import static com.github.bekk.exampleapp.tasks.OneTimeTaskExample.SEND_WELCOME_EMAIL;

import com.github.bekk.exampleapp.model.NewSignup;
import com.github.bekk.exampleapp.model.Order;
import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Keeps the dashboard alive for demos. Every tick it schedules a few one-time instances at small
 * random offsets, reusing the other example tasks so that successes, failures, retries, chained
 * tasks and long-running tasks all keep appearing over time.
 *
 * <p>This is itself a db-scheduler recurring task, so the simulator and everything it spawns show
 * up in the UI it is meant to populate.
 */
@Configuration
public class DemoTrafficSimulatorTask {

  public static final TaskDescriptor<Void> SIMULATE_TRAFFIC = TaskDescriptor.of("simulate-traffic");

  private final AtomicLong counter = new AtomicLong();

  @Bean
  public Task<?> simulateTraffic() {
    return Tasks.recurring(SIMULATE_TRAFFIC, FixedDelay.ofSeconds(25))
        .execute(
            (inst, ctx) -> {
              final SchedulerClient client = ctx.getSchedulerClient();
              final Random random = new Random();
              final int events = 1 + random.nextInt(3); // 1..3 events this tick
              for (int i = 0; i < events; i++) {
                final Instant when = Instant.now().plusSeconds(random.nextInt(20)); // 0..19s jitter
                scheduleRandomEvent(client, random, when);
              }
            });
  }

  private void scheduleRandomEvent(SchedulerClient client, Random random, Instant when) {
    final long n = counter.incrementAndGet();
    switch (random.nextInt(6)) {
      case 0, 1 -> // most common: a new signup welcome email (succeeds)
          client.scheduleIfNotExists(
              SEND_WELCOME_EMAIL
                  .instance("sim-user-" + n)
                  .data(
                      new NewSignup(
                          n, "newsignup-" + n + "@mail.com", "New Signup " + n, Instant.now()))
                  .build(),
              when);
      case 2 -> // order chain: capture payment -> ship package
          client.scheduleIfNotExists(
              ORDER_CAPTURE_PAYMENT
                  .instance("sim-order-" + n)
                  .data(
                      new Order("sim-order-" + n, "Sim Customer " + n, 19.99 + random.nextInt(480)))
                  .build(),
              when);
      case 3 -> // long-running invoice generation
          client.scheduleIfNotExists(
              GENERATE_MONTHLY_INVOICE_PDF.instance("sim-invoice-" + n).build(), when);
      case 4 -> // always-failing card charge
          client.scheduleIfNotExists(CHARGE_CREDIT_CARD.instance("sim-charge-" + n).build(), when);
      default -> // failing webhook with exponential-backoff retries
          client.scheduleIfNotExists(
              DELIVER_OUTGOING_WEBHOOK.instance("sim-webhook-" + n).build(), when);
    }
  }
}
