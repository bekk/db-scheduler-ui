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
package com.github.bekk.exampleapp.service;

import static com.github.bekk.exampleapp.tasks.ChainTask.ORDER_CAPTURE_PAYMENT;
import static com.github.bekk.exampleapp.tasks.FailingTask.CHARGE_CREDIT_CARD;
import static com.github.bekk.exampleapp.tasks.FailingTask.DELIVER_OUTGOING_WEBHOOK;
import static com.github.bekk.exampleapp.tasks.LongRunningTask.GENERATE_MONTHLY_INVOICE_PDF;
import static com.github.bekk.exampleapp.tasks.OneTimeTaskExample.SEND_WELCOME_EMAIL;

import com.github.bekk.exampleapp.model.TaskData;
import com.github.bekk.exampleapp.model.TestObject;
import com.github.kagkarlsson.scheduler.Scheduler;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

  private final Scheduler scheduler;

  public TaskService(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  public void runManuallyTriggeredTasks() {
    scheduler.schedule(
        SEND_WELCOME_EMAIL
            .instance("user-10472")
            .data(new TaskData(10472, "ole.nordman@mail.com", Instant.now()))
            .build(),
        Instant.now());

    scheduler.schedule(
        ORDER_CAPTURE_PAYMENT
            .instance("order-88314")
            .data(new TestObject("Ole Nordman", 1, "ole.nordman@mail.com"))
            .build(),
        Instant.now());

    for (int i = 1; i <= 5; i++) {
      scheduler.schedule(
          GENERATE_MONTHLY_INVOICE_PDF.instance("invoice-2026-05-acme-" + i).build(),
          Instant.now().plusSeconds(i * 10L));
    }

    scheduler.schedule(
        CHARGE_CREDIT_CARD.instance("order-2026-05-22-8831").build(), Instant.now().plusSeconds(2));

    scheduler.schedule(
        DELIVER_OUTGOING_WEBHOOK.instance("evt_3PqL9k2a-customer-created").build(),
        Instant.now().plusSeconds(2));

    scheduler.schedule(
        SEND_WELCOME_EMAIL
            .instance("delete-1")
            .data(new TaskData(-1, "task 1 to delete", Instant.now()))
            .build(),
        Instant.now());

    scheduler.schedule(
        SEND_WELCOME_EMAIL
            .instance("delete-2")
            .data(new TaskData(-2, "task 2 to delete", Instant.now()))
            .build(),
        Instant.now());
  }
}
