package com.github.bekk.exampleapp.tasks;

import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskWithoutDataDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;

import static utils.Utils.sleep;

public class LongRunningTask {

    public static final TaskWithoutDataDescriptor LONG_RUNNING_ONETIME_TASK =
            new TaskWithoutDataDescriptor("long-running-task");

    public static final TaskWithoutDataDescriptor LONG_RUNNING_RECURRING_TASK =
            new TaskWithoutDataDescriptor("long-running-recurring-task");

    public static Task<?> runLongRunningTask() {
        return Tasks.oneTime(LONG_RUNNING_ONETIME_TASK)
                .execute((inst, ctx) -> {
                    System.out.println("Executing long running task: " + inst.getTaskName());
                    sleep(10000);
                });
    }

    public static Task<?> runLongRunningRecurringTask(){
        return Tasks.recurring(LONG_RUNNING_RECURRING_TASK, FixedDelay.ofSeconds(20))
                .execute((inst, ctx) -> {
                    System.out.println("Executing long running recurring task: " + inst.getTaskName());
                    sleep(10000);
                });
    }
}
