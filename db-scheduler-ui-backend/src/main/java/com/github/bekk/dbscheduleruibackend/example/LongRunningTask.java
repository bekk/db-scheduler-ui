package com.github.bekk.dbscheduleruibackend.example;

import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;

public class LongRunningTask {
    public static Task<?> runLongRunningTask() {
        return Tasks.oneTime("long-running-task", Void.class)
                .execute((inst, ctx) -> {
                    System.out.println("Executing long running task: " + inst.getTaskName());
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }

    public static Task<?> runLongRunningRecurringTask(){
        return Tasks.recurring("long-running-recurring-task", com.github.kagkarlsson.scheduler.task.schedule.FixedDelay.ofSeconds(20))
                .execute((inst, ctx) -> {
                    System.out.println("Executing long running recurring task: " + inst.getTaskName());
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }
}
