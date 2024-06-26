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
import com.github.kagkarlsson.scheduler.boot.config.DbSchedulerCustomizer;
import com.github.kagkarlsson.scheduler.serializer.Serializer;
import javax.sql.DataSource;
import no.bekk.dbscheduler.ui.controller.ConfigController;
import no.bekk.dbscheduler.ui.controller.LogController;
import no.bekk.dbscheduler.ui.controller.TaskController;
import no.bekk.dbscheduler.ui.controller.UIController;
import no.bekk.dbscheduler.ui.model.DbSchedulerUiConfig;
import no.bekk.dbscheduler.ui.service.LogLogic;
import no.bekk.dbscheduler.ui.service.TaskLogic;
import no.bekk.dbscheduler.ui.util.Caching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(value = "db-scheduler-ui.enabled", matchIfMissing = true)
public class UiApiAutoConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(UiApiAutoConfiguration.class);

  @Value("${db-scheduler-ui.task-data:true}")
  boolean showTaskData;

  @Value("${db-scheduler-ui.history:false}")
  boolean showHistory;

  UiApiAutoConfiguration() {
    logger.info("UiApiAutoConfiguration created");
  }

  @Bean
  @ConditionalOnMissingBean
  Caching caching() {
    return new Caching();
  }

  @Bean
  @ConditionalOnMissingBean
  TaskLogic taskLogic(Scheduler scheduler, Caching caching) {
    return new TaskLogic(scheduler, caching, showTaskData);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(
      prefix = "db-scheduler-ui",
      name = "history",
      havingValue = "true",
      matchIfMissing = false)
  LogLogic logLogic(DataSource dataSource, Caching caching, DbSchedulerCustomizer customizer) {
    return new LogLogic(
        dataSource,
        customizer.serializer().orElse(Serializer.DEFAULT_JAVA_SERIALIZER),
        caching,
        showTaskData);
  }

  @Bean
  @ConditionalOnMissingBean
  TaskController taskController(TaskLogic taskLogic) {
    return new TaskController(taskLogic);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(
      prefix = "db-scheduler-ui",
      name = "history",
      havingValue = "true",
      matchIfMissing = false)
  LogController logController(LogLogic logLogic) {
    return new LogController(logLogic);
  }

  @Bean
  @ConditionalOnMissingBean
  DbSchedulerUiConfig dbSchedulerUiConfig() {
    return new DbSchedulerUiConfig(showHistory);
  }

  @Bean
  @ConditionalOnMissingBean
  UIController uiController() {
    return new UIController();
  }

  @Bean
  @ConditionalOnMissingBean
  ConfigController configController() {
    return new ConfigController();
  }
}
