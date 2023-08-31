package com.github.bekk.exampleapp.config;

import com.github.bekk.exampleapp.tasks.TaskDefinitions;
import com.github.kagkarlsson.scheduler.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.time.Duration;

@Configuration
public class SchedulerConfig {
    private final DataSource dataSource;

    @Autowired
    public SchedulerConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public Scheduler scheduler() {
        return Scheduler.create(dataSource, TaskDefinitions.getAllKnownTaskDefinitions())
                .startTasks()
                .pollingInterval(Duration.ofSeconds(5))
                .registerShutdownHook()
                .build();
    }
}
