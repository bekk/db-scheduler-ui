package com.github.bekk.exampleapp.tasks;

import com.github.bekk.exampleapp.model.TaskData;
import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;

import java.time.Instant;

import static java.lang.Thread.sleep;

public class ChainTask {

        public static OneTimeTask<TaskData> chainTaskStepOne() {
            return Tasks.oneTime("chained-onetime-task-step1", TaskData.class).
                    execute((inst, ctx) ->{
                        final SchedulerClient client = ctx.getSchedulerClient();
                        System.out.println("Executed chained onetime task step 1: " + inst.getTaskName()+ " " + inst.getId());
                        client.schedule(chainTaskStepTwo().instance(inst.getId()), Instant.now());
                    });
        }

        public static OneTimeTask<TaskData> chainTaskStepTwo(){
            return Tasks.oneTime("chained-onetime-task-step2", TaskData.class).
                    execute((inst, ctx) -> {
                        try {
                            sleep(5000);
                            System.out.println("Executed chained onetime task step 2: " + inst.getTaskName() + " " + inst.getId());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }
