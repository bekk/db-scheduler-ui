package no.bekk.dbscheduler.ui.util;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public interface AndCondition {
  String getQueryPart();

  void setParameters(MapSqlParameterSource parameterSource);
}
