package com.github.bekk.exampleapp.config;

import com.github.bekk.exampleapp.tasks.TaskDefinitions;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.serializer.JavaSerializer;
import io.rocketbase.extension.jdbc.JdbcLogRepository;
import io.rocketbase.extension.jdbc.Snowflake;
import io.rocketbase.extension.stats.LogStatsPlainRegistry;
import java.time.Duration;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerConfig {
  private final DataSource dataSource;

  @Autowired
  public SchedulerConfig(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Bean
  public JdbcLogRepository jdbcLogRepository() {
    return new JdbcLogRepository(
        dataSource, new JavaSerializer(), JdbcLogRepository.DEFAULT_TABLE_NAME, new Snowflake());
  }

  @Bean
  public Scheduler scheduler() {
    return Scheduler.create(dataSource, TaskDefinitions.getAllKnownTaskDefinitions())
        .startTasks()
        .pollingInterval(Duration.ofSeconds(5))
        .statsRegistry(new LogStatsPlainRegistry(jdbcLogRepository()))
        .registerShutdownHook()
        .build();
  }
}
