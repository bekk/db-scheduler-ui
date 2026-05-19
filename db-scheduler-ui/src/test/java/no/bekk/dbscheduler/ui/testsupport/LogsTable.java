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
package no.bekk.dbscheduler.ui.testsupport;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public final class LogsTable {

  public static final String NAME = "scheduled_execution_logs";

  private static final AtomicLong ID_SEQ = new AtomicLong();

  private final JdbcTemplate jdbc;

  public LogsTable(DataSource dataSource) {
    this.jdbc = new JdbcTemplate(dataSource);
  }

  static void createSchema(DataSource dataSource) {
    new JdbcTemplate(dataSource)
        .execute(
            "create table "
                + NAME
                + " ("
                + " id bigint not null primary key,"
                + " task_name text not null,"
                + " task_instance text not null,"
                + " task_data bytea,"
                + " picked_by text,"
                + " time_started timestamp with time zone not null,"
                + " time_finished timestamp with time zone not null,"
                + " succeeded boolean not null,"
                + " duration_ms bigint not null,"
                + " exception_class text,"
                + " exception_message text,"
                + " exception_stacktrace text"
                + ")");
  }

  public void insert(LogsRow row) {
    Timestamp now = Timestamp.from(Instant.now());
    jdbc.update(
        "insert into "
            + NAME
            + " (id, task_name, task_instance, picked_by, time_started, time_finished,"
            + " succeeded, duration_ms) values (?, ?, ?, ?, ?, ?, ?, ?)",
        ID_SEQ.incrementAndGet(),
        row.taskName,
        row.taskInstance,
        "test",
        now,
        now,
        row.succeeded,
        0L);
  }
}
