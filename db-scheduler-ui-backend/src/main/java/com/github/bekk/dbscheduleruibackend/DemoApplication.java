package com.github.bekk.dbscheduleruibackend;

import com.github.bekk.dbscheduleruibackend.service.TaskService;
import com.github.kagkarlsson.scheduler.Scheduler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public CommandLineRunner runAllTasks(Scheduler scheduler) {
		return args -> {
			System.out.println("Running all tasks");
			TaskService taskService = new TaskService(scheduler);
			taskService.runAllTasks();
		};
	}
}
