package com.github.bekk.exampleapp.tasks;

import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;

import java.util.Random;

public class RecurringTaskExample {
    public static RecurringTask<Void> getExample() {
        return Tasks.recurring("example-recurring-task" + 1, FixedDelay.ofSeconds(6))
                .execute(
                        (inst, ctx) -> {
                            try {
                                Thread.sleep(2200);
                                if (new Random().nextInt(100) < 30) {
                                    throw new RuntimeException("Simulated failure in example recurring task");
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            };
                            System.out.println("Executed recurring task: " + inst.getTaskName());
                        });
    }
}
