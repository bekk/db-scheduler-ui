/*
 * Copyright (C) Bekk
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.bekk.exampleapp.tasks;

import com.github.bekk.exampleapp.model.TaskData;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OneTimeTaskExample {

  public static final TaskDescriptor<TaskData> ONE_TIME_TASK =
      TaskDescriptor.of("onetime-task", TaskData.class);

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
