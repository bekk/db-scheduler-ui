package com.github.bekk.exampleapp.tasks;


import com.github.kagkarlsson.scheduler.task.FailureHandler;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;

import static java.time.Duration.ofSeconds;

public class FailingTask {
    public static Task<?> runOneTimeFailing() {
        return Tasks.oneTime("failing-one-time-task", Void.class)
                .execute((inst, ctx) -> {
                    throw new RuntimeException("Simulated task failure");
                });
    }

    public static Task<?> runRecurringFailing() {
        return Tasks.recurring("failing-recurring-task", FixedDelay.ofSeconds(6))
                .execute((inst, ctx) -> {
                    throw new RuntimeException("Simulated task failure");
                });
    }

    public static Task<?> runOneTimeFailingWithBackoff() {
        return Tasks.oneTime("failing-one-time-with-backoff-task", Void.class)
                .onFailure(new FailureHandler.ExponentialBackoffFailureHandler<>(ofSeconds(1)))
                .execute((inst, ctx) -> {
                    throw new RuntimeException("Simulated task failure");
                });
    }
}
