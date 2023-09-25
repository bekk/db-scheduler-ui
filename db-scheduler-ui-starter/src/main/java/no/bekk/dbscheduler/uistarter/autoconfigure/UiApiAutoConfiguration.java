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
package no.bekk.dbscheduler.uistarter.autoconfigure;

import com.github.kagkarlsson.scheduler.Scheduler;
import javax.sql.DataSource;
import no.bekk.dbscheduler.ui.controller.LogController;
import no.bekk.dbscheduler.ui.controller.TaskController;
import no.bekk.dbscheduler.ui.controller.UIController;
import no.bekk.dbscheduler.ui.service.LogLogic;
import no.bekk.dbscheduler.ui.service.TaskLogic;
import no.bekk.dbscheduler.ui.util.Caching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class UiApiAutoConfiguration {

  @Value("${db-scheduler-ui.task-data}")
  private boolean showTaskData;
  @Value("${db-scheduler-ui.history}")
  private boolean showHistory;
  private static final Logger logger = LoggerFactory.getLogger(UiApiAutoConfiguration.class);

  public UiApiAutoConfiguration() {
    logger.info("UiApiAutoConfiguration created");
  }

  @Bean
  @ConditionalOnMissingBean
  public Caching caching() {
    return new Caching();
  }

  @Bean
  @ConditionalOnMissingBean
  public TaskLogic taskLogic(Scheduler scheduler, Caching caching) {
    return new TaskLogic(scheduler, caching);
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
    return new UIController(showTaskData, showHistory);
  }
}
