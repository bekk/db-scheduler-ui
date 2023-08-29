package com.github.bekk.dbscheduleruibackend.example;

import com.github.bekk.dbscheduleruibackend.model.TaskData;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;

public class OneTimeTaskExample {
    public static OneTimeTask<TaskData> getExample() {
        return Tasks.oneTime("example-onetime-task", TaskData.class).
                execute((inst,ctx) -> {
                    System.out.println("Executed onetime task: " + inst.getTaskName());
                    System.out.println("With data id: " + inst.getData().id() + " data: " + inst.getData().data());
                });
    }
}
