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
import no.bekk.dbscheduler.ui.testsupport.LogsRow;
import no.bekk.dbscheduler.ui.testsupport.LogsTable;
import no.bekk.dbscheduler.ui.testsupport.TaskDetailsRequestParamsBuilder;
import no.bekk.dbscheduler.ui.testsupport.TestDatabase;
import no.bekk.dbscheduler.ui.util.Caching;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LogLogicTest {

  private DataSource dataSource;
  private LogLogic logLogic;
  private LogsTable logsTable;

  @BeforeEach
  void setUp() {
    dataSource = TestDatabase.newDataSourceWithTables();
    logLogic = new LogLogic(dataSource, null, new Caching(), false, LogsTable.NAME, 0);
    logsTable = new LogsTable(dataSource);
  }

  @Test
  void storesAndRetrievesLogEntry() {
    logsTable.insert(LogsRow.defaultRow().succeeded(true).build());

    List<LogModel> logs =
        logLogic.getLogsDirectlyFromDB(TaskDetailsRequestParamsBuilder.builder().build());

    assertThat(logs)
        .singleElement()
        .satisfies(
            log -> {
              assertThat(log.getTaskName()).isEqualTo("happy-task");
              assertThat(log.getTaskInstance()).isEqualTo("happy-task-1");
              assertThat(log.isSucceeded()).isTrue();
            });
  }
}
