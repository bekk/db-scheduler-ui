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
package no.bekk.dbscheduler.ui.service;

import com.github.kagkarlsson.scheduler.jdbc.AutodetectJdbcCustomization;
import com.github.kagkarlsson.scheduler.jdbc.JdbcCustomization;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import no.bekk.dbscheduler.ui.model.InstanceHistory.InstanceRun;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Reads the run log for one instance.
 *
 * <p>Separate from {@link LogLogic} on purpose: that one serves the History page — paging,
 * free-text search, filters, a shared cache and a 500-row ceiling. All this needs is the newest few
 * rows for one exact instance.
 *
 * <p>Both queries filter on {@code (task_name, task_instance)} and read newest-first, which the
 * {@code stl_task_instance_idx} index in {@code sql/log-table/*.sql} serves directly. Without that
 * index they degrade to scanning every log row belonging to the task.
 *
 * <p>The stack trace is fetched only for the one failed row that gets shown, so the common case —
 * an instance that is running fine — never pulls one over the wire.
 */
public class InstanceLogRepository {

  private static final String COLUMNS =
      "id, succeeded, time_started, duration_ms, exception_class, exception_message";

  private final DataSource dataSource;
  private final String logTableName;

  // time_started is written through setInstant; read it back the same way so the encodings match.
  private final JdbcCustomization jdbcCustomization;

  public InstanceLogRepository(DataSource dataSource, String logTableName) {
    this.dataSource = dataSource;
    this.logTableName = logTableName;
    this.jdbcCustomization = new AutodetectJdbcCustomization(dataSource);
  }

  /** The most recent runs for one instance, newest first. */
  public List<InstanceRun> findRecentRuns(String taskName, String instanceId, int limit) {
    return query(
        select("null as exception_stacktrace", ""), parameters(taskName, instanceId), limit);
  }

  /** The most recent failed run, with its stack trace, or empty if this instance never failed. */
  public Optional<InstanceRun> findLastFailure(String taskName, String instanceId) {
    return query(
            select("exception_stacktrace", " and succeeded = :succeeded"),
            // Bound, not inlined: `false` is not a literal every database accepts — Oracle
            // stores this as a number and SQL Server as a bit.
            parameters(taskName, instanceId).addValue("succeeded", false),
            1)
        .stream()
        .findFirst();
  }

  private String select(String traceColumn, String extraCondition) {
    return "select "
        + COLUMNS
        + ", "
        + traceColumn
        + " from "
        + logTableName
        + " where task_name = :taskName and task_instance = :instanceId"
        + extraCondition
        + " order by id desc";
  }

  private List<InstanceRun> query(String sql, MapSqlParameterSource parameters, int maxRows) {
    return template(maxRows).query(sql, parameters, this::mapRun);
  }

  private static MapSqlParameterSource parameters(String taskName, String instanceId) {
    return new MapSqlParameterSource()
        .addValue("taskName", taskName)
        .addValue("instanceId", instanceId);
  }

  // Row-capping via JDBC rather than a LIMIT clause, because `limit` / `top` / `fetch first` are
  // not the same word everywhere. A template per call because setMaxRows mutates it, and these
  // are shared across requests.
  private NamedParameterJdbcTemplate template(int maxRows) {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.setMaxRows(maxRows);
    return new NamedParameterJdbcTemplate(jdbcTemplate);
  }

  private InstanceRun mapRun(ResultSet resultSet, int rowNum) throws SQLException {
    return new InstanceRun(
        resultSet.getLong("id"),
        resultSet.getBoolean("succeeded"),
        jdbcCustomization.getInstant(resultSet, "time_started"),
        resultSet.getLong("duration_ms"),
        resultSet.getString("exception_class"),
        resultSet.getString("exception_message"),
        resultSet.getString("exception_stacktrace"));
  }
}
