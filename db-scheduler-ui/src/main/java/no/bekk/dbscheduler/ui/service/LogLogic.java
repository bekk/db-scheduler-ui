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

import com.github.kagkarlsson.scheduler.serializer.Serializer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import no.bekk.dbscheduler.ui.model.GetLogsResponse;
import no.bekk.dbscheduler.ui.model.LogModel;
import no.bekk.dbscheduler.ui.model.LogPollResponse;
import no.bekk.dbscheduler.ui.model.TaskDetailsRequestParams;
import no.bekk.dbscheduler.ui.model.TaskRequestParams;
import no.bekk.dbscheduler.ui.util.AndCondition;
import no.bekk.dbscheduler.ui.util.Caching;
import no.bekk.dbscheduler.ui.util.QueryBuilder;
import no.bekk.dbscheduler.ui.util.QueryUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class LogLogic {

  private static final int DEFAULT_LIMIT = 500;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private final Caching caching;
  private final LogModelRowMapper logModelRowMapper;
  private final String logTableName;

  public LogLogic(
      DataSource dataSource,
      Serializer serializer,
      Caching caching,
      boolean showData,
      String logTableName) {
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    // currently we have no paging in the UI
    this.namedParameterJdbcTemplate.getJdbcTemplate().setMaxRows(DEFAULT_LIMIT);
    this.caching = caching;
    this.logTableName = logTableName;
    this.logModelRowMapper =
        new LogModelRowMapper(
            showData, serializer == null ? Serializer.DEFAULT_JAVA_SERIALIZER : serializer);
  }

  public GetLogsResponse getLogs(TaskDetailsRequestParams requestParams) {
    List<LogModel> logs =
        caching.getLogsFromCacheOrDB(requestParams.isRefresh(), this, requestParams);
    List<LogModel> pagedLogs =
        QueryUtils.paginate(logs, requestParams.getPageNumber(), requestParams.getSize());

    return new GetLogsResponse(logs.size(), pagedLogs, requestParams.getSize());
  }

  public LogPollResponse pollLogs(TaskDetailsRequestParams requestParams) {
    List<LogModel> logsFromDB = getLogsDirectlyFromDB(requestParams);

    long newFailures =
        logsFromDB.stream()
            .filter(log -> !caching.checkLogCacheForKey(log.getId()) && !log.isSucceeded())
            .count();

    long newSucceeded =
        logsFromDB.stream()
            .filter(log -> !caching.checkLogCacheForKey(log.getId()) && log.isSucceeded())
            .count();

    return new LogPollResponse((int) newFailures, (int) newSucceeded);
  }

  public List<LogModel> getLogsDirectlyFromDB(TaskDetailsRequestParams requestParams) {
    QueryBuilder queryBuilder = QueryBuilder.selectFromTable(logTableName);
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
              "time_finished",
              Operators.LESS_THAN_OR_EQUALS.getOperator(),
              requestParams.getEndTime()));
    }
    if (requestParams.getFilter() != null
        && requestParams.getFilter() != TaskRequestParams.TaskFilter.ALL) {
      queryBuilder.andCondition(new FilterCondition(requestParams.getFilter()));
    }
    if (requestParams.getSearchTermTaskName() != null) {
      queryBuilder.andCondition(
          new SearchCondition(
              requestParams.getSearchTermTaskName(),
              new HashMap<>(),
              true,
              requestParams.isTaskNameExactMatch()));
    }

    if (requestParams.getSearchTermTaskInstance() != null) {
      queryBuilder.andCondition(
          new SearchCondition(
              requestParams.getSearchTermTaskInstance(),
              new HashMap<>(),
              false,
              requestParams.isTaskInstanceExactMatch()));
    }

    queryBuilder.orderBy(requestParams.isAsc() ? "time_finished desc" : "time_finished asc");

    return namedParameterJdbcTemplate.query(
        queryBuilder.getQuery(), queryBuilder.getParameters(), logModelRowMapper);
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
      p.addValue(varName, value == null ? null : Timestamp.from(value));
    }
  }

  private static class SearchCondition implements AndCondition {

    private final String searchTerm;
    private final Map<String, Object> params;

    private final boolean isTaskName;

    private final boolean isExactMatch;

    public SearchCondition(
        String searchTerm, Map<String, Object> params, boolean isTaskName, boolean isExactMatch) {
      this.searchTerm = searchTerm;
      this.params = params;
      this.isTaskName = isTaskName;
      this.isExactMatch = isExactMatch;
    }

    @Override
    public String getQueryPart() {
      return QueryUtils.logSearchCondition(searchTerm, params, isTaskName, isExactMatch);
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

  @RequiredArgsConstructor
  public static class LogModelRowMapper implements RowMapper<LogModel> {

    private final boolean showData;
    private final Serializer serializer;

    @Override
    public LogModel mapRow(ResultSet rs, int rowNum) throws SQLException {
      Object taskData = null;
      if (showData) {
        final byte[] bytes = rs.getBytes("task_data");
        if (bytes != null) {
          taskData = serializer.deserialize(Object.class, bytes);
        }
      }

      return new LogModel(
          rs.getLong("id"),
          rs.getString("task_name"),
          rs.getString("task_instance"),
          taskData,
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
