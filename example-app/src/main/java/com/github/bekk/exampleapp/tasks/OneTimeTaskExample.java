package com.github.bekk.exampleapp.tasks;

import com.github.bekk.exampleapp.model.TaskData;
import com.github.kagkarlsson.scheduler.task.TaskWithDataDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;

import static utils.Utils.sleep;

public class OneTimeTaskExample {

    public static final TaskWithDataDescriptor<TaskData> ONE_TIME_TASK =
            new TaskWithDataDescriptor<>("onetime-task", TaskData.class);

    public static OneTimeTask<TaskData> getExample() {
        return Tasks.oneTime(ONE_TIME_TASK).
                execute((inst,ctx) -> {
                    sleep(300);
                    System.out.println("Executed onetime task: " + inst.getTaskName());
                    System.out.println("With data id: " + inst.getData().getId() + " data: " + inst.getData().getData());
                });
    }
}
