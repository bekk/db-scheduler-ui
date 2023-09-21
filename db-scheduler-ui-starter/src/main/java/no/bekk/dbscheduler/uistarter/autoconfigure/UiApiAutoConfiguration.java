package no.bekk.dbscheduler.uistarter.autoconfigure;

import no.bekk.dbscheduler.ui.controller.LogController;
import no.bekk.dbscheduler.ui.controller.TaskController;
import no.bekk.dbscheduler.ui.controller.UIController;
import no.bekk.dbscheduler.ui.service.LogLogic;
import no.bekk.dbscheduler.ui.service.TaskLogic;
import com.github.kagkarlsson.scheduler.Scheduler;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class UiApiAutoConfiguration {
  private static final Logger logger = LoggerFactory.getLogger(UiApiAutoConfiguration.class);

  public UiApiAutoConfiguration() {
    logger.info("UiApiAutoConfiguration created");
  }

  @Bean
  @ConditionalOnMissingBean
  public TaskLogic taskLogic(Scheduler scheduler) {
    return new TaskLogic(scheduler);
  }

  @Bean
  @ConditionalOnMissingBean
  public LogLogic logLogic(DataSource dataSource) {
    return new LogLogic(dataSource);
  }

  @Bean
  @ConditionalOnMissingBean
  public TaskController taskController(TaskLogic taskLogic) {
    return new TaskController(taskLogic);
  }

  @Bean
  @ConditionalOnMissingBean
  public LogController logController(LogLogic logLogic) {
    return new LogController(logLogic);
  }

  @Bean
  @ConditionalOnMissingBean
  public UIController uiController() {
    return new UIController();
  }
}