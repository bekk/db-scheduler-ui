package com.github.bekk.dbscheduleruistarter.autoconfigure;

import com.github.bekk.dbscheduleruiapi.controller.LogController;
import com.github.bekk.dbscheduleruiapi.controller.TaskController;
import com.github.bekk.dbscheduleruiapi.controller.UIController;
import com.github.bekk.dbscheduleruiapi.service.LogLogic;
import com.github.bekk.dbscheduleruiapi.service.TaskLogic;
import com.github.kagkarlsson.scheduler.Scheduler;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class UiApiAutoConfiguration {

  @Value("${db-scheduler-ui.taskdata:true}")
  public boolean data;

  private static final Logger logger = LoggerFactory.getLogger(UiApiAutoConfiguration.class);

  public UiApiAutoConfiguration() {
    logger.info("UiApiAutoConfiguration created");
  }

  @Bean
  @ConditionalOnMissingBean
  public TaskLogic taskLogic(Scheduler scheduler) {
    return new TaskLogic(scheduler, data);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(
      prefix = "db-scheduler-ui",
      name = "history",
      havingValue = "true",
      matchIfMissing = true)
  public LogLogic logLogic(DataSource dataSource) {
    return new LogLogic(dataSource, data);
  }

  @Bean
  @ConditionalOnMissingBean
  public TaskController taskController(TaskLogic taskLogic) {
    return new TaskController(taskLogic);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(
      prefix = "db-scheduler-ui",
      name = "history",
      havingValue = "true",
      matchIfMissing = true)
  public LogController logController(LogLogic logLogic) {
    return new LogController(logLogic);
  }

  @Bean
  @ConditionalOnMissingBean
  public UIController uiController() {
    return new UIController();
  }
}
