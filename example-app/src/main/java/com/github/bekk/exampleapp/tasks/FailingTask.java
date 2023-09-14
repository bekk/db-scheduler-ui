package com.github.bekk.exampleapp.tasks;

import static java.time.Duration.ofSeconds;

import com.github.kagkarlsson.scheduler.task.FailureHandler;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskWithoutDataDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FailingTask {

  public static final TaskWithoutDataDescriptor FAILING_ONETIME_TASK =
      new TaskWithoutDataDescriptor("failing-one-time-task");

  public static final TaskWithoutDataDescriptor FAILING_RECURRING_TASK =
      new TaskWithoutDataDescriptor("failing-recurring-task");

  public static final TaskWithoutDataDescriptor FAILING_ONETIME_TASK_BACKOFF =
      new TaskWithoutDataDescriptor("failing-one-time-with-backoff-task");

  @Bean
  public Task<?> runOneTimeFailing() {
    return Tasks.oneTime(FAILING_ONETIME_TASK)
        .execute(
            (inst, ctx) -> {
              throw new RuntimeException("Simulated task failure");
            });
  }

  @Bean
  public Task<?> runRecurringFailing() {
    return Tasks.recurring(FAILING_RECURRING_TASK, FixedDelay.ofSeconds(6))
        .execute(
            (inst, ctx) -> {
              throw new RuntimeException("Simulated task failure");
            });
  }

  @Bean
  public Task<?> runOneTimeFailingWithBackoff() {
    return Tasks.oneTime(FAILING_ONETIME_TASK_BACKOFF)
        .onFailure(new FailureHandler.ExponentialBackoffFailureHandler<>(ofSeconds(1)))
        .execute(
            (inst, ctx) -> {
              throw new RuntimeException("Simulated task failure");
            });
  }
}
