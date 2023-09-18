package com.github.bekk.dbscheduleruiapi.service;

import com.github.bekk.dbscheduleruiapi.model.LogModel;
import com.github.bekk.dbscheduleruiapi.model.TaskDetailsRequestParams;
import com.github.bekk.dbscheduleruiapi.util.QueryUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LogLogic {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Autowired
  public LogLogic(DataSource dataSource) {
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
  }

  public List<LogModel> getLogsById(TaskDetailsRequestParams requestParams) {
    Map<String, Object> params = new HashMap<>();
    params.put("taskName", requestParams.getTaskName());
    params.put("taskInstance", requestParams.getTaskId());
    StringBuilder baseQuery =
        new StringBuilder(
            "SELECT * FROM scheduled_execution_logs WHERE task_name = :taskName AND task_instance = :taskInstance");

    List<String> conditions = new ArrayList<>();
    QueryUtils.logSearchCondition(params, requestParams.getSearchTerm(), conditions);
    QueryUtils.logFilterCondition(requestParams.getFilter(), conditions);

    if (!conditions.isEmpty()) {
      baseQuery.append(" AND ").append(String.join(" AND ", conditions));
    }

    baseQuery.append(" ORDER BY time_started DESC");
    return namedParameterJdbcTemplate.query(baseQuery.toString(), params, new LogModelRowMapper());
  }

  public List<LogModel> getAllLogs(TaskDetailsRequestParams requestParams) {
    StringBuilder baseQuery = new StringBuilder("SELECT * FROM scheduled_execution_logs");
    Map<String, Object> params = new HashMap<>();
    List<String> conditions = new ArrayList<>();

    QueryUtils.logSearchCondition(params, requestParams.getSearchTerm(), conditions);
    QueryUtils.logFilterCondition(requestParams.getFilter(), conditions);

    if (!conditions.isEmpty()) {
      baseQuery.append(" WHERE ").append(String.join(" AND ", conditions));
    }

    baseQuery.append(" LIMIT 20");
    return namedParameterJdbcTemplate.query(baseQuery.toString(), params, new LogModelRowMapper());
  }

  public static class LogModelRowMapper implements RowMapper<LogModel> {

    @Override
    public LogModel mapRow(ResultSet rs, int rowNum) throws SQLException {
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
