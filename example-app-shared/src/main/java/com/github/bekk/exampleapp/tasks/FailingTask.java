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
import com.github.kagkarlsson.scheduler.task.schedule.Schedules;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FailingTask {

  public static final TaskDescriptor<Void> CHARGE_CREDIT_CARD =
      TaskDescriptor.of("charge-credit-card");

  public static final TaskDescriptor<Void> RECONCILE_LEDGER_WITH_BANK =
      TaskDescriptor.of("reconcile-ledger-with-bank");

  public static final TaskDescriptor<Void> DELIVER_OUTGOING_WEBHOOK =
      TaskDescriptor.of("deliver-outgoing-webhook");

  @Bean
  public Task<?> chargeCreditCard() {
    return Tasks.oneTime(CHARGE_CREDIT_CARD)
        .execute(
            (inst, ctx) -> {
              throw new RuntimeException("Card declined: insufficient_funds");
            });
  }

  @Bean
  public Task<?> reconcileLedgerWithBank() {
    return Tasks.recurring(RECONCILE_LEDGER_WITH_BANK, Schedules.cron("0 */5 * * * *"))
        .execute(
            (inst, ctx) -> {
              throw new RuntimeException("Bank statement endpoint timed out");
            });
  }

  @Bean
  public Task<?> deliverOutgoingWebhook() {
    return Tasks.oneTime(DELIVER_OUTGOING_WEBHOOK)
        .onFailure(new FailureHandler.ExponentialBackoffFailureHandler<>(ofSeconds(2)))
        .execute(
            (inst, ctx) -> {
              throw new RuntimeException("Subscriber returned HTTP 500");
            });
  }
}
