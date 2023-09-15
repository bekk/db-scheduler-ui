package com.github.bekk.exampleapp.tasks;

import static utils.Utils.sleep;

import com.github.bekk.exampleapp.model.TestObject;
import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskWithDataDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChainTask {

  public static final TaskWithDataDescriptor<TestObject> CHAINED_STEP_1_TASK =
      new TaskWithDataDescriptor<>("chained-step-1", TestObject.class);

  public static final TaskWithDataDescriptor<TestObject> CHAINED_STEP_2_TASK =
      new TaskWithDataDescriptor<>("chained-step-2", TestObject.class);

  @Bean
  public Task<TestObject> chainTaskStepOne() {
    return Tasks.oneTime(CHAINED_STEP_1_TASK)
        .execute(
            (inst, ctx) -> {
              sleep(5000);
              final SchedulerClient client = ctx.getSchedulerClient();
              System.out.println(
                  "Executed chained onetime task step 1: "
                      + inst.getTaskName()
                      + " "
                      + inst.getId());
              TestObject data = inst.getData();
              data.setId(data.getId() + 1);
              client.schedule(
                  CHAINED_STEP_2_TASK.instance(inst.getId(), data), Instant.now().plusSeconds(10));
            });
  }

  @Bean
  public Task<TestObject> chainTaskStepTwo() {
    return Tasks.oneTime(CHAINED_STEP_2_TASK)
        .execute(
            (inst, ctx) -> {
              sleep(5000);
              final SchedulerClient client = ctx.getSchedulerClient();
              System.out.println(
                  "Executed chained onetime task step 2: "
                      + inst.getTaskName()
                      + " "
                      + inst.getId());
              TestObject data = inst.getData();
              data.setId(data.getId() + 1);
              client.schedule(
                  CHAINED_STEP_1_TASK.instance(inst.getId(), data), Instant.now().plusSeconds(20));
            });
  }
}
