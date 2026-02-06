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
package com.github.bekk.exampleapp;

import com.github.bekk.exampleapp.service.TaskService;
import com.github.kagkarlsson.scheduler.Scheduler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ExampleApp {
  public static void main(String[] args) {
    SpringApplication.run(ExampleApp.class, args);
  }

  @Bean
  public CommandLineRunner runAllManuallyTriggeredTasks(Scheduler scheduler) {
    return args -> {
      System.out.println("Running all manually triggered tasks");
      TaskService taskService = new TaskService(scheduler);
      taskService.runManuallyTriggeredTasks();
    };
  }
}
