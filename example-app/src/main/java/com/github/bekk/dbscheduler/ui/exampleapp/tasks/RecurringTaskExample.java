package com.github.bekk.dbscheduler.ui.exampleapp.tasks;

import static utils.Utils.sleep;

import com.github.kagkarlsson.scheduler.task.TaskWithoutDataDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import java.util.Random;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecurringTaskExample {

  public static final TaskWithoutDataDescriptor RECURRING_TASK =
      new TaskWithoutDataDescriptor("recurring-task");

  @Bean
  public RecurringTask<Void> getExample() {
    return Tasks.recurring(RECURRING_TASK, FixedDelay.ofSeconds(6))
        .execute(
            (inst, ctx) -> {
              sleep(5000);
              if (new Random().nextInt(100) < 30) {
                throw new RuntimeException("Simulated failure in example recurring task");
              }

              System.out.println("Executed recurring task: " + inst.getTaskName());
            });
  }
}
