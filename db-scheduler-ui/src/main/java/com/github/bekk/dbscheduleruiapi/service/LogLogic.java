package com.github.bekk.dbscheduleruiapi.service;

import com.github.bekk.dbscheduleruiapi.model.LogModel;
import com.github.bekk.dbscheduleruiapi.model.TaskDetailsRequestParams;
import com.github.bekk.dbscheduleruiapi.model.TaskRequestParams;
import com.github.bekk.dbscheduleruiapi.util.AndCondition;
import com.github.bekk.dbscheduleruiapi.util.QueryBuilder;
import com.github.bekk.dbscheduleruiapi.util.QueryUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LogLogic {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private static final int DEFAULT_LIMIT = 500;

  @Autowired
  public LogLogic(DataSource dataSource) {
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
  }

  public List<LogModel> getLogs(TaskDetailsRequestParams requestParams) {
    QueryBuilder queryBuilder = QueryBuilder.selectFromTable("scheduled_execution_logs");
    if (requestParams.getStartTime() != null) {
      queryBuilder.andCondition(
          new TimeCondition(
              "time_started",
              Operators.GREATER_THAN_OR_EQUALS.getOperator(),
              requestParams.getStartTime()));
    }
    if (requestParams.getEndTime() != null) {
      queryBuilder.andCondition(
          new TimeCondition(
              "time_started",
              Operators.LESS_THAN_OR_EQUALS.getOperator(),
              requestParams.getEndTime()));
    }
    if (requestParams.getFilter() != null
        && requestParams.getFilter() != TaskRequestParams.TaskFilter.ALL) {
      queryBuilder.andCondition(new FilterCondition(requestParams.getFilter()));
    }
    if (requestParams.getSearchTerm() != null) {
      queryBuilder.andCondition(
          new SearchCondition(requestParams.getSearchTerm(), new HashMap<>()));
    }

    queryBuilder.orderBy("time_started DESC");

    queryBuilder.limit(DEFAULT_LIMIT);

    return namedParameterJdbcTemplate.query(
        queryBuilder.getQuery(), queryBuilder.getParameters(), new LogModelRowMapper());
  }

  private static class TimeCondition implements AndCondition {
    private final String varName;
    private final String operator;
    private final Instant value;

    public TimeCondition(String varName, String operator, Instant value) {
      this.varName = varName;
      this.operator = operator;
      this.value = value;
    }

    @Override
    public String getQueryPart() {
      return varName + " " + operator + " :" + varName;
    }

    @Override
    public void setParameters(MapSqlParameterSource p) {
      p.addValue(varName, value);
    }
  }

  private static class SearchCondition implements AndCondition {
    private final String searchTerm;
    private final Map<String, Object> params;

    public SearchCondition(String searchTerm, Map<String, Object> params) {
      this.searchTerm = searchTerm;
      this.params = params;
    }

    @Override
    public String getQueryPart() {
      return QueryUtils.logSearchCondition(searchTerm, params);
    }

    @Override
    public void setParameters(MapSqlParameterSource p) {
      for (Map.Entry<String, Object> entry : params.entrySet()) {
        p.addValue(entry.getKey(), entry.getValue());
      }
    }
  }

  public static class FilterCondition implements AndCondition {
    private final TaskRequestParams.TaskFilter filterCondition;

    public FilterCondition(TaskRequestParams.TaskFilter filterCondition) {
      this.filterCondition = filterCondition;
    }

    @Override
    public String getQueryPart() {
      return filterCondition == TaskRequestParams.TaskFilter.SUCCEEDED
          ? "succeeded = TRUE"
          : "succeeded = FALSE";
    }

    @Override
    public void setParameters(MapSqlParameterSource p) {
      p.addValue("filterCondition", filterCondition);
    }
  }

  public static class LogModelRowMapper implements RowMapper<LogModel> {

    @Override
    public LogModel mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new LogModel(
          rs.getLong("id"),
          rs.getString("task_name"),
          rs.getString("task_instance"),
          rs.getBytes("task_data"),
          rs.getTimestamp("time_started").toInstant(),
          rs.getTimestamp("time_finished").toInstant(),
          rs.getBoolean("succeeded"),
          rs.getLong("duration_ms"),
          rs.getString("exception_class"),
          rs.getString("exception_message"),
          rs.getString("exception_stacktrace"));
    }
  }

  private enum Operators {
    GREATER_THAN_OR_EQUALS(">="),
    LESS_THAN_OR_EQUALS("<=");

    private final String operator;

    Operators(String operator) {
      this.operator = operator;
    }

    public String getOperator() {
      return operator;
    }
  }
}
