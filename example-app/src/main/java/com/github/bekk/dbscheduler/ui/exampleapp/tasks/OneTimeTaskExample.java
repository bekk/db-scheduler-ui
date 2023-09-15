package com.github.bekk.dbscheduler.ui.exampleapp.tasks;

import com.github.bekk.dbscheduler.ui.exampleapp.model.TaskData;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskWithDataDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OneTimeTaskExample {

  public static final TaskWithDataDescriptor<TaskData> ONE_TIME_TASK =
      new TaskWithDataDescriptor<>("onetime-task", TaskData.class);

  @Bean
  public Task<TaskData> exampleOneTimeTask() {
    return Tasks.oneTime(ONE_TIME_TASK)
        .execute(
            (inst, ctx) -> {
              System.out.println("Executed onetime task: " + inst.getTaskName());
              System.out.println(
                  "With data id: " + inst.getData().getId() + " data: " + inst.getData().getData());
            });
  }
}
