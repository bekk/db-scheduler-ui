package com.github.bekk.dbscheduleruiapi.service;

import com.github.bekk.dbscheduleruiapi.model.LogModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LogLogic {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public LogLogic(DataSource dataSource) {
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  public List<LogModel> getLogsById(String taskName, String taskInstance) {
    Map<String, Object> params = new HashMap<>();
    params.put("taskName", taskName);
    params.put("taskInstance", taskInstance);
    return namedParameterJdbcTemplate.query(
        "SELECT * FROM scheduled_execution_logs WHERE task_name = :taskName AND task_instance = :taskInstance ORDER BY time_started DESC",
        params,
        new LogModelRowMapper());
  }

  public List<LogModel> getAllLogs() {
    return jdbcTemplate.query(
        "SELECT * FROM scheduled_execution_logs LIMIT 500", new LogModelRowMapper());
  }

  public static class LogModelRowMapper implements RowMapper<LogModel> {

    @Override
    public LogModel mapRow(ResultSet rs, int rowNum) throws SQLException {
      long id = rs.getLong("id");
      String taskName = rs.getString("task_name");
      String taskInstance = rs.getString("task_instance");
      String taskData = rs.getString("task_data");
      Instant timeStarted = rs.getTimestamp("time_started").toInstant();
      Instant timeFinished = rs.getTimestamp("time_finished").toInstant();
      boolean succeeded = rs.getBoolean("succeeded");
      Long durationMs = rs.getLong("duration_ms");
      String exceptionClass = rs.getString("exception_class");
      String exceptionMessage = rs.getString("exception_message");
      String exceptionStackTrace = rs.getString("exception_stacktrace");
      return new LogModel(
          rs.getLong("id"),
          rs.getString("task_name"),
          rs.getString("task_instance"),
          rs.getString("task_data"),
          rs.getTimestamp("time_started").toInstant(),
          rs.getTimestamp("time_finished").toInstant(),
          rs.getBoolean("succeeded"),
          rs.getLong("duration_ms"),
          rs.getString("exception_class"),
          rs.getString("exception_message"),
          rs.getString("exception_stacktrace"));
    }
  }
}
