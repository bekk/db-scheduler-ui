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

import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LongRunningTask {

  public static final TaskDescriptor<Void> GENERATE_MONTHLY_INVOICE_PDF =
      TaskDescriptor.of("generate-monthly-invoice-pdf");

  public static final TaskDescriptor<Void> REBUILD_SEARCH_INDEX =
      TaskDescriptor.of("rebuild-search-index");

  @Bean
  public Task<?> generateMonthlyInvoicePdf() {
    return Tasks.oneTime(GENERATE_MONTHLY_INVOICE_PDF)
        .execute(
            (inst, ctx) -> {
              System.out.println("Generating monthly invoice PDF: " + inst.getId());
              sleep(10000);
            });
  }

  @Bean
  public Task<?> rebuildSearchIndex() {
    return Tasks.recurring(REBUILD_SEARCH_INDEX, FixedDelay.ofMinutes(5))
        .execute(
            (inst, ctx) -> {
              System.out.println("Rebuilding search index");
              sleep(30000);
            });
  }
}
