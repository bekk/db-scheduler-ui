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
package com.github.bekk.exampleappmicronaut;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

@Factory
public class SchedulerConfiguration {

  public static final TaskDescriptor<NewSignup> SEND_WELCOME_EMAIL =
      TaskDescriptor.of("send-welcome-email", NewSignup.class);

  private static final String CREATE_TABLE =
      """
      create table if not exists scheduled_tasks (
          task_name varchar(255),
          task_instance varchar(255),
          task_data BYTEA,
          execution_time TIMESTAMP WITH TIME ZONE,
          picked BIT,
          picked_by varchar(50),
          last_success TIMESTAMP WITH TIME ZONE,
          last_failure TIMESTAMP WITH TIME ZONE,
          consecutive_failures INT,
          last_heartbeat TIMESTAMP WITH TIME ZONE,
          version BIGINT,
          PRIMARY KEY (task_name, task_instance)
      );
      """;

  @Singleton
  DataSource dataSource() {
    JdbcDataSource dataSource = new JdbcDataSource();
    dataSource.setURL(
        "jdbc:h2:mem:db-scheduler-ui;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;"
            + "DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1");
    dataSource.setUser("sa");
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute(CREATE_TABLE);
    } catch (SQLException e) {
      throw new IllegalStateException("Failed to initialise the db-scheduler schema", e);
    }
    return dataSource;
  }

  @Singleton
  Task<NewSignup> sendWelcomeEmail() {
    return Tasks.oneTime(SEND_WELCOME_EMAIL)
        .execute(
            (inst, ctx) -> {
              NewSignup data = inst.getData();
              System.out.println(
                  "Sending welcome email to "
                      + data.getCustomerName()
                      + " <"
                      + data.getEmail()
                      + ">");
            });
  }

  @Singleton
  Scheduler scheduler(DataSource dataSource, List<Task<?>> tasks) {
    return Scheduler.create(dataSource, tasks).registerShutdownHook().build();
  }
}
