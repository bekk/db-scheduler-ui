/*
 * Copyright (C) Marten Prieß
 * Originally part of https://github.com/rocketbase-io/db-scheduler-log
 * Copyright (C) Bekk (modifications)
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
package no.bekk.dbscheduler.ui.log.jdbc;

import com.github.kagkarlsson.scheduler.jdbc.AutodetectJdbcCustomization;
import com.github.kagkarlsson.scheduler.jdbc.JdbcCustomization;
import com.github.kagkarlsson.scheduler.serializer.Serializer;
import java.io.NotSerializableException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Objects;
import javax.sql.DataSource;
import no.bekk.dbscheduler.ui.log.ExecutionLog;
import no.bekk.dbscheduler.ui.log.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcLogRepository implements LogRepository {

  public static final String DEFAULT_TABLE_NAME = "scheduled_execution_logs";

  private static final Logger LOG = LoggerFactory.getLogger(JdbcLogRepository.class);

  private final JdbcTemplate jdbcTemplate;
  private final Serializer serializer;
  private final String tableName;
  private final JdbcCustomization jdbcCustomization;
  private final IdProvider idProvider;

  public JdbcLogRepository(
      DataSource dataSource, Serializer serializer, String tableName, IdProvider idProvider) {
    this.tableName = tableName;
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.serializer = Objects.requireNonNull(serializer, "serializer");
    this.jdbcCustomization = new AutodetectJdbcCustomization(dataSource);
    this.idProvider = idProvider;
  }

  @Override
  public boolean createIfNotExists(ExecutionLog log) {
    try {
      jdbcTemplate.update(
          "insert into "
              + tableName
              + "(id, task_name, task_instance, task_data, picked_by, time_started, time_finished,"
              + " succeeded, duration_ms, exception_class, exception_message, exception_stacktrace)"
              + " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
          p -> {
            p.setLong(1, idProvider.nextId());
            p.setString(2, log.taskInstance().getTaskName());
            p.setString(3, log.taskInstance().getId());
            p.setObject(4, serialize(log.taskInstance().getData()));
            p.setString(5, log.pickedBy());
            jdbcCustomization.setInstant(p, 6, log.timeStarted());
            jdbcCustomization.setInstant(p, 7, log.timeFinished());
            p.setBoolean(8, log.succeeded());
            p.setLong(9, Duration.between(log.timeStarted(), log.timeFinished()).toMillis());
            p.setString(10, log.cause() != null ? log.cause().getClass().getName() : null);
            p.setString(11, log.cause() != null ? log.cause().getMessage() : null);
            p.setString(12, getStacktrace(log.cause()));
          });
      return true;
    } catch (DuplicateKeyException e) {
      LOG.debug("Skipping duplicate execution-log row (id collision): {}", e.getMessage());
      return false;
    } catch (DataAccessException e) {
      LOG.error("Failed to insert execution-log row", e);
      return false;
    }
  }

  private String getStacktrace(Throwable cause) {
    if (cause == null) {
      return null;
    }
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    cause.printStackTrace(out);
    return writer.toString();
  }

  private byte[] serialize(Object value) {
    if (value == null) {
      return null;
    }
    try {
      return serializer.serialize(value);
    } catch (Exception e) {
      if (e instanceof NotSerializableException) {
        LOG.warn("object is not serializable - you need to add Serializable");
      } else {
        LOG.error("serialization failed for {} -> {}", value.getClass(), e.getMessage());
      }
      return null;
    }
  }
}
