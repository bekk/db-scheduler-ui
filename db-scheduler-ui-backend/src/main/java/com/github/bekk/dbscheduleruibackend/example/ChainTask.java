package com.github.bekk.dbscheduleruibackend.example;

import com.github.bekk.dbscheduleruibackend.model.TestObject;
import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import java.time.Instant;
import static java.lang.Thread.sleep;

public class ChainTask {

        public static OneTimeTask<TestObject> chainTaskStepOne() {
            return Tasks.oneTime("chained-onetime-task-step1", TestObject.class).
                    execute((inst, ctx) ->{
                        try {
                            sleep(500);
                            final SchedulerClient client = ctx.getSchedulerClient();
                            System.out.println("Executed chained onetime task step 1: " + inst.getTaskName()+ " " + inst.getId());
                            TestObject data = inst.getData();
                            data.setId(data.getId()+1);
                            client.schedule(chainTaskStepTwo().instance(inst.getId(), data), Instant.now());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    });
        }

        public static OneTimeTask<TestObject> chainTaskStepTwo(){
            return Tasks.oneTime("chained-onetime-task-step2", TestObject.class).
                    execute((inst, ctx) -> {
                        try {
                            sleep(500);
                            final SchedulerClient client = ctx.getSchedulerClient();
                            System.out.println("Executed chained onetime task step 2: " + inst.getTaskName() + " " + inst.getId());
                            TestObject data = inst.getData();
                            data.setId(data.getId()+1);
                            client.schedule(chainTaskStepOne().instance(inst.getId(),data), Instant.now());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }
