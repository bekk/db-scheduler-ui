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

import com.github.bekk.exampleapp.model.TestObject;
import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChainTask {

  public static final TaskDescriptor<TestObject> ORDER_CAPTURE_PAYMENT =
      TaskDescriptor.of("order-capture-payment", TestObject.class);

  public static final TaskDescriptor<TestObject> ORDER_SHIP_PACKAGE =
      TaskDescriptor.of("order-ship-package", TestObject.class);

  @Bean
  public Task<TestObject> orderCapturePayment() {
    return Tasks.oneTime(ORDER_CAPTURE_PAYMENT)
        .execute(
            (inst, ctx) -> {
              sleep(2000);
              final SchedulerClient client = ctx.getSchedulerClient();
              System.out.println("Captured payment for order: " + inst.getId());
              TestObject data = inst.getData();
              data.setId(data.getId() + 1);
              client.scheduleIfNotExists(
                  ORDER_SHIP_PACKAGE.instance(inst.getId()).data(data).build(),
                  Instant.now().plusSeconds(5));
            });
  }

  @Bean
  public Task<TestObject> orderShipPackage() {
    return Tasks.oneTime(ORDER_SHIP_PACKAGE)
        .execute(
            (inst, ctx) -> {
              sleep(2000);
              System.out.println("Shipped package for order: " + inst.getId());
            });
  }
}
