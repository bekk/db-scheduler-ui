package com.github.bekk.exampleapp.tasks;

import com.github.bekk.exampleapp.model.TaskData;
import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskWithDataDescriptor;
import com.github.kagkarlsson.scheduler.task.TaskWithoutDataDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import static utils.Utils.sleep;

public class SpawnerTask {

    public static final TaskWithoutDataDescriptor RECURRING_SPAWNER_TASK =
            new TaskWithoutDataDescriptor("recurring-spawner-task");

    public static final TaskWithDataDescriptor<TaskData> ONE_TIME_SPAWNER_TASK =
            new TaskWithDataDescriptor<>("onetime-spawned-task", TaskData.class);

    public static Task<?> runSpawner(){
        return Tasks.recurring(RECURRING_SPAWNER_TASK, FixedDelay.ofSeconds(180))
                .execute((inst, ctx) -> {
                    final SchedulerClient client = ctx.getSchedulerClient();
                    final long randomUUID = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;

                    for(int i = 0; i < 100; i++){
                        client.schedule(ONE_TIME_SPAWNER_TASK.instance("spawned " + randomUUID + " loopnr: " + i,
                                new TaskData(123,"{data: MASSIVEDATA}")), Instant.now().plusSeconds(60));
                    }
                });
    }

    public static Task<TaskData> runOneTimeSpawned (){
        return Tasks.oneTime(ONE_TIME_SPAWNER_TASK)
                .execute((inst, ctx) -> {
                        sleep(10000);
                        if (new Random().nextInt(100) < 20) {
                            throw new RuntimeException("Simulated failure");
                        }
                    });

    }
}
