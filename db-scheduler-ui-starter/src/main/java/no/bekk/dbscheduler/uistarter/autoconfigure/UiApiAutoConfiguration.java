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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

@AutoConfiguration
public class UiApiAutoConfiguration {
  private static final Logger logger = LoggerFactory.getLogger(UiApiAutoConfiguration.class);
  @Autowired private ResourceLoader resourceLoader;

  @Value("${db-scheduler-ui.taskdata:true}")
  public boolean showTaskData;

  @Value("${db-scheduler-ui.history:false}")
  private boolean showHistory;

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
    return new TaskLogic(scheduler, caching, showTaskData);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(
      prefix = "db-scheduler-ui",
      name = "history",
      havingValue = "true",
      matchIfMissing = false)
  public LogLogic logLogic(DataSource dataSource) {
    return new LogLogic(dataSource, showTaskData);
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
      matchIfMissing = false)
  public LogController logController(LogLogic logLogic) {
    return new LogController(logLogic);
  }

  @Bean
  @ConditionalOnMissingBean
  public UIController uiController() {
    return new UIController(showTaskData, showHistory);
  }
}
