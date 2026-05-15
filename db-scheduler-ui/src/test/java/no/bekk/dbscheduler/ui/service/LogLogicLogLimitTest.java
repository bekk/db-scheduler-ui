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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import javax.sql.DataSource;
import no.bekk.dbscheduler.ui.model.LogModel;
import no.bekk.dbscheduler.ui.model.TaskDetailsRequestParams;
import no.bekk.dbscheduler.ui.testsupport.LogsTable;
import no.bekk.dbscheduler.ui.testsupport.TaskDetailsRequestParamsBuilder;
import no.bekk.dbscheduler.ui.testsupport.TestDatabase;
import no.bekk.dbscheduler.ui.util.Caching;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Reproduces the bug where the {@code logLimit} configuration is silently capped at 500 because
 * the {@link LogLogic} constructor calls {@code JdbcTemplate.setMaxRows(500)} regardless of the
 * configured limit.
 */
class LogLogicLogLimitTest {

  private static final String TASK_NAME = "log-limit-bug-test-task";

  private static DataSource dataSource;

  @BeforeAll
  static void setUpSchema() {
    dataSource = TestDatabase.newDataSourceWithTables();
  }

  @Test
  void should_return_more_than_500_rows() {
    int rowsToInsert = 600;
    int rowsFetchLimit = 1000;
    LogsTable.insertRows(dataSource, TASK_NAME, rowsToInsert);
    LogLogic logLogic = buildLogLogic(rowsFetchLimit);

    TaskDetailsRequestParams params =
        TaskDetailsRequestParamsBuilder.builder()
            .searchTermTaskName(TASK_NAME)
            .taskNameExactMatch(true)
            .size(rowsToInsert)
            .build();

    List<LogModel> logs = logLogic.getLogsDirectlyFromDB(params);

    assertThat(logs).hasSize(rowsToInsert);
  }

  private static LogLogic buildLogLogic(int rowsFetchLimit) {
    LogLogic logLogic =
        new LogLogic(
            dataSource,
            /* serializer= */ null,
            new Caching(),
            /* showData= */ false,
            LogsTable.NAME,
            rowsFetchLimit);
    return logLogic;
  }
}
