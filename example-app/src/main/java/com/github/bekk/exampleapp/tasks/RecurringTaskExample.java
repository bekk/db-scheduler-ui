package com.github.bekk.exampleapp.tasks;

import com.github.kagkarlsson.scheduler.task.TaskWithoutDataDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;

import java.util.Random;

import static utils.Utils.sleep;


public class RecurringTaskExample {

    public static final TaskWithoutDataDescriptor RECURRING_TASK =
            new TaskWithoutDataDescriptor("recurring-task");

    public static RecurringTask<Void> getExample() {
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
