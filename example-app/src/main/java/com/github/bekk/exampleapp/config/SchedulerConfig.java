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
package com.github.bekk.exampleapp.config;

import com.github.kagkarlsson.scheduler.serializer.JavaSerializer;
import io.rocketbase.extension.jdbc.JdbcLogRepository;
import io.rocketbase.extension.jdbc.Snowflake;
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
}
