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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import javax.sql.DataSource;
import no.bekk.dbscheduler.ui.model.*;
import no.bekk.dbscheduler.ui.util.AndCondition;
import no.bekk.dbscheduler.ui.util.Caching;
import no.bekk.dbscheduler.ui.util.QueryBuilder;
import no.bekk.dbscheduler.ui.util.QueryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LogLogic {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private static boolean showData;
  private static final int DEFAULT_LIMIT = 500;
  @Autowired private Caching caching;


  @Autowired
  public LogLogic(DataSource dataSource, boolean showData) {
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    this.showData = showData;
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
              "time_finished",
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
    queryBuilder.limit(500);
    queryBuilder.orderBy(requestParams.isAsc() ? "time_finished desc" : "time_finished asc");

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
      byte[] taskData = null;
      if(showData){
        taskData = rs.getBytes("task_data");
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
