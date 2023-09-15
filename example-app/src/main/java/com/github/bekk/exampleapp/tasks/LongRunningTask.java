package com.github.bekk.exampleapp.tasks;

import static utils.Utils.sleep;

import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskWithoutDataDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LongRunningTask {

  public static final TaskWithoutDataDescriptor LONG_RUNNING_ONETIME_TASK =
      new TaskWithoutDataDescriptor("long-running-task");

  public static final TaskWithoutDataDescriptor LONG_RUNNING_RECURRING_TASK =
      new TaskWithoutDataDescriptor("long-running-recurring-task");

  @Bean
  public Task<?> runLongRunningTask() {
    return Tasks.oneTime(LONG_RUNNING_ONETIME_TASK)
        .execute(
            (inst, ctx) -> {
              System.out.println("Executing long running task: " + inst.getTaskName());
              sleep(10000);
            });
  }

  @Bean
  public Task<?> runLongRunningRecurringTask() {
    return Tasks.recurring(LONG_RUNNING_RECURRING_TASK, FixedDelay.ofSeconds(20))
        .execute(
            (inst, ctx) -> {
              System.out.println("Executing long running recurring task: " + inst.getTaskName());
              sleep(10000);
            });
  }
}
