package com.github.bekk.exampleapp.tasks;

import com.github.bekk.exampleapp.model.TaskData;
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
