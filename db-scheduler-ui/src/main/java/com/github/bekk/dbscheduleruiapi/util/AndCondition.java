package com.github.bekk.dbscheduleruiapi.util;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public interface AndCondition {
  String getQueryPart();

  void setParameters(MapSqlParameterSource parameterSource);
}
