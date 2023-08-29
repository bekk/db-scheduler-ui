package com.github.bekk.dbscheduleruibackend.example;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;

import java.time.Instant;
import java.util.UUID;

public class SpawnerTask {
    public static Task<?> runSpawner(){
        return Tasks.recurring("recurring-spawner-task", FixedDelay.ofSeconds(20))
                .execute((inst, ctx) -> {
                    final SchedulerClient client = ctx.getSchedulerClient();
                    final long randomUUID = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;

                    System.out.println("Scheduling spawned executions.");
                    for(int i = 0; i < 5; i++){
                        client.schedule(runSpawned().instance("spawned " + randomUUID + " loopnr: " + i), Instant.now().plusSeconds(15));
                    }
                });
    }

    public static Task<?> runSpawned (){
        return Tasks.oneTime("onetime-spawned-task", Void.class)
                .execute((inst, ctx) -> System.out.println("Executed spawned task: " + inst.getTaskName()));

    }
}
