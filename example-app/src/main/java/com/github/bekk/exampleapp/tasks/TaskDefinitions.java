package com.github.bekk.exampleapp.tasks;

import com.github.bekk.exampleapp.model.TaskData;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.bekk.exampleapp.model.TestObject;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;

import java.util.List;

import static com.github.bekk.exampleapp.tasks.ChainTask.CHAINED_STEP_1_TASK;
import static com.github.bekk.exampleapp.tasks.FailingTask.*;
import static com.github.bekk.exampleapp.tasks.LongRunningTask.*;
import static com.github.bekk.exampleapp.tasks.OneTimeTaskExample.ONE_TIME_TASK;
import static com.github.bekk.exampleapp.tasks.RecurringTaskExample.RECURRING_TASK;
import static com.github.bekk.exampleapp.tasks.SpawnerTask.RECURRING_SPAWNER_TASK;

public class TaskDefinitions {

    public static List<Task<?>> getAllKnownTaskDefinitions() {
        return List.of(
                OneTimeTaskExample.getExample(),
                RecurringTaskExample.getExample(),
                ChainTask.chainTaskStepOne(),
                ChainTask.chainTaskStepTwo(),
                LongRunningTask.runLongRunningTask(),
                FailingTask.runOneTimeFailing(),
                FailingTask.runRecurringFailing(),
                FailingTask.runOneTimeFailingWithBackoff(),
                SpawnerTask.runSpawner(),
                SpawnerTask.runOneTimeSpawned(),
                LongRunningTask.runLongRunningRecurringTask()
        );
    }

    public static List<TaskInstance<?>> getAllKnownTaskInstances() {
        return List.of(
                ONE_TIME_TASK.instance("1", new TaskData(1, "test data")),
                RECURRING_TASK.instance(RecurringTask.INSTANCE),
                CHAINED_STEP_1_TASK.instance("3", new TestObject("Ole Nordman", 1, "ole.nordman@mail.com")),
                LONG_RUNNING_ONETIME_TASK.instance("5"),
                LONG_RUNNING_RECURRING_TASK.instance(RecurringTask.INSTANCE),
                FAILING_ONETIME_TASK.instance("6"),
                FAILING_RECURRING_TASK.instance(RecurringTask.INSTANCE),
                FAILING_ONETIME_TASK_BACKOFF.instance("8"),
                RECURRING_SPAWNER_TASK.instance(RecurringTask.INSTANCE)
        );
    }
}
