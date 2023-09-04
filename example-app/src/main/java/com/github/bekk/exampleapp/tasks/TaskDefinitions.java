package com.github.bekk.exampleapp.tasks;

import com.github.bekk.exampleapp.model.TaskData;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.bekk.exampleapp.model.TestObject;

import java.util.List;

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
                SpawnerTask.runSpawned(),
                LongRunningTask.runLongRunningRecurringTask()
        );
    }

    public static List<TaskInstance<?>> getAllKnownTaskInstances() {
        return List.of(
                OneTimeTaskExample.getExample().instance("1", new TaskData(1, "test data")),
                RecurringTaskExample.getExample().instance("2"),
                ChainTask.chainTaskStepOne().instance("3", new TestObject("Ole Nordman", 3, "ole.nordman@mail.com")),
                LongRunningTask.runLongRunningTask().instance("5"),
                FailingTask.runOneTimeFailing().instance("6"),
                FailingTask.runRecurringFailing().instance("7"),
                FailingTask.runOneTimeFailingWithBackoff().instance("8"),
                SpawnerTask.runSpawner().instance("9"),
                SpawnerTask.runSpawned().instance("10"),
                LongRunningTask.runLongRunningRecurringTask().instance("11")
        );
    }
}
