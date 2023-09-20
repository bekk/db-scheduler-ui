package no.bekk.dbscheduler.ui.util;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class QueryBuilder {
  private final String tableName;

  private final List<AndCondition> andConditions = new ArrayList<>();

  private Optional<String> orderBy = empty();

  private Optional<Integer> limit = empty();

  QueryBuilder(String tableName) {
    this.tableName = tableName;
  }

  public static QueryBuilder selectFromTable(String tableName) {
    return new QueryBuilder(tableName);
  }

  public QueryBuilder andCondition(AndCondition andCondition) {
    andConditions.add(andCondition);
    return this;
  }

  public QueryBuilder orderBy(String orderBy) {
    this.orderBy = Optional.of(orderBy);
    return this;
  }

  public QueryBuilder limit(int limit) {
    this.limit = Optional.of(limit);
    return this;
  }

  public String getQuery() {
    StringBuilder s = new StringBuilder();
    s.append("select * from ").append(tableName);

    if (!andConditions.isEmpty()) {
      s.append(" where ");
      s.append(andConditions.stream().map(AndCondition::getQueryPart).collect(joining(" and ")));
    }

    orderBy.ifPresent(o -> s.append(" order by ").append(o));

    limit.ifPresent(l -> s.append(" limit ").append(l));

    return s.toString();
  }

  public MapSqlParameterSource getParameters() {
    MapSqlParameterSource parameterSource = new MapSqlParameterSource();
    andConditions.forEach(c -> c.setParameters(parameterSource));
    return parameterSource;
  }
}
