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

import static java.time.Duration.ofMinutes;

import com.github.bekk.exampleapp.model.InvoiceEmail;
import com.github.kagkarlsson.scheduler.task.FailureHandler;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * A one-time task with hundreds of instances, some of them failing, so the task page has enough
 * rows to page through and every status tab has something in it.
 */
@Configuration
public class InvoiceEmailTask {

  public static final TaskDescriptor<InvoiceEmail> SEND_INVOICE_EMAIL =
      TaskDescriptor.of("send-invoice-email", InvoiceEmail.class);

  @Bean
  public Task<?> sendInvoiceEmail() {
    return Tasks.oneTime(SEND_INVOICE_EMAIL)
        // Retried half an hour later, so failures stay visible while the example runs.
        .onFailure(new FailureHandler.OnFailureRetryLater<>(ofMinutes(30)))
        .execute(
            (instance, context) -> {
              if (instance.getId().endsWith("7")) {
                throw new RuntimeException("SMTP server rejected recipient");
              }
            });
  }
}
