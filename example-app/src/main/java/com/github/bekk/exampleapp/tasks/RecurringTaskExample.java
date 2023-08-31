package com.github.bekk.exampleapp.tasks;

import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;

public class RecurringTaskExample {
    public static RecurringTask<Void> getExample() {
        return Tasks.recurring("example-recurring-task" + 1, FixedDelay.ofSeconds(6))
                .execute(
                        (inst, ctx) -> System.out.println("Executed recurring task: " + inst.getTaskName()));
    }
}
