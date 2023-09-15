package com.github.bekk.dbscheduler.ui.exampleapp;

import com.github.bekk.dbscheduler.ui.exampleapp.service.TaskService;
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
