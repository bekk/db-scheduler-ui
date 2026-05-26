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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import javax.sql.DataSource;
import no.bekk.dbscheduler.ui.model.GetLogsResponse;
import no.bekk.dbscheduler.ui.model.LogModel;
import no.bekk.dbscheduler.ui.model.LogPollResponse;
import no.bekk.dbscheduler.ui.model.TaskDetailsRequestParams;
import no.bekk.dbscheduler.ui.model.TaskRequestParams.TaskFilter;
import no.bekk.dbscheduler.ui.testsupport.LogsRow;
import no.bekk.dbscheduler.ui.testsupport.LogsTable;
import no.bekk.dbscheduler.ui.testsupport.TestDatabase;
import no.bekk.dbscheduler.ui.util.Caching;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LogLogicTest {

  private DataSource dataSource;
  private Caching caching;
  private LogLogic logLogic;
  private LogsTable logsTable;

  @BeforeEach
  void setUp() {
    dataSource = TestDatabase.newDataSourceWithTables();
    caching = new Caching();
    logLogic = new LogLogic(dataSource, null, caching, false, LogsTable.NAME, 0);
    logsTable = new LogsTable(dataSource);
  }

  // --- Retrieval ---

  @Test
  void getLogsDirectlyFromDB_singleEntry_returnsInsertedLog() {
    logsTable.insert(LogsRow.defaultRow().build());

    List<LogModel> logs = logLogic.getLogsDirectlyFromDB(defaultRequest());

    assertThat(logs)
        .singleElement()
        .satisfies(
            log -> {
              assertThat(log.getTaskName()).isEqualTo("happy-task");
              assertThat(log.getTaskInstance()).isEqualTo("happy-task-1");
              assertThat(log.isSucceeded()).isTrue();
            });
  }

  @Test
  void getLogsDirectlyFromDB_emptyDatabase_returnsEmpty() {
    assertThat(logLogic.getLogsDirectlyFromDB(defaultRequest())).isEmpty();
  }

  @Test
  void getLogsDirectlyFromDB_withLogLimit_capsReturnedRows() {
    for (int i = 0; i < 5; i++) {
      logsTable.insert(LogsRow.defaultRow().build());
    }
    LogLogic limited = new LogLogic(dataSource, null, caching, false, LogsTable.NAME, 2);

    List<LogModel> logs = limited.getLogsDirectlyFromDB(defaultRequest());

    assertThat(logs).hasSize(2);
  }

  // --- Filter ---

  @Test
  void getLogsDirectlyFromDB_failedFilter_returnsOnlyFailedLogs() {
    logsTable.insert(LogsRow.defaultRow().taskName("ok-job").succeeded(true).build());
    logsTable.insert(LogsRow.defaultRow().taskName("failing-job").succeeded(false).build());

    List<LogModel> logs =
        logLogic.getLogsDirectlyFromDB(
            TaskDetailsRequestParams.builder().filter(TaskFilter.FAILED).build());

    assertThat(logs).extracting(LogModel::getTaskName).containsExactly("failing-job");
  }

  @Test
  void getLogsDirectlyFromDB_succeededFilter_returnsOnlySucceededLogs() {
    logsTable.insert(LogsRow.defaultRow().taskName("ok-job").succeeded(true).build());
    logsTable.insert(LogsRow.defaultRow().taskName("failing-job").succeeded(false).build());

    List<LogModel> logs =
        logLogic.getLogsDirectlyFromDB(
            TaskDetailsRequestParams.builder().filter(TaskFilter.SUCCEEDED).build());

    assertThat(logs).extracting(LogModel::getTaskName).containsExactly("ok-job");
  }

  @Test
  void getLogsDirectlyFromDB_allFilter_returnsBothSucceededAndFailedLogs() {
    logsTable.insert(LogsRow.defaultRow().taskName("ok-job").succeeded(true).build());
    logsTable.insert(LogsRow.defaultRow().taskName("failing-job").succeeded(false).build());

    List<LogModel> logs =
        logLogic.getLogsDirectlyFromDB(
            TaskDetailsRequestParams.builder().filter(TaskFilter.ALL).build());

    assertThat(logs)
        .extracting(LogModel::getTaskName)
        .containsExactlyInAnyOrder("ok-job", "failing-job");
  }

  // --- Time range ---

  @Test
  void getLogsDirectlyFromDB_startTimeFilter_returnsLogsAtOrAfterTime() {
    Instant now = Instant.now();
    logsTable.insert(
        LogsRow.defaultRow().taskName("old").time(now.minus(Duration.ofHours(2))).build());
    logsTable.insert(LogsRow.defaultRow().taskName("new").time(now).build());

    List<LogModel> logs =
        logLogic.getLogsDirectlyFromDB(
            TaskDetailsRequestParams.builder().startTime(now.minus(Duration.ofHours(1))).build());

    assertThat(logs).extracting(LogModel::getTaskName).containsExactly("new");
  }

  @Test
  void getLogsDirectlyFromDB_endTimeFilter_returnsLogsAtOrBeforeTime() {
    Instant now = Instant.now();
    logsTable.insert(
        LogsRow.defaultRow().taskName("old").time(now.minus(Duration.ofHours(2))).build());
    logsTable.insert(LogsRow.defaultRow().taskName("new").time(now).build());

    List<LogModel> logs =
        logLogic.getLogsDirectlyFromDB(
            TaskDetailsRequestParams.builder().endTime(now.minus(Duration.ofHours(1))).build());

    assertThat(logs).extracting(LogModel::getTaskName).containsExactly("old");
  }

  @Test
  void getLogsDirectlyFromDB_startAndEndTimeRange_returnsLogsInsideRange() {
    Instant now = Instant.now();
    logsTable.insert(
        LogsRow.defaultRow().taskName("old").time(now.minus(Duration.ofHours(3))).build());
    logsTable.insert(
        LogsRow.defaultRow().taskName("inside").time(now.minus(Duration.ofMinutes(45))).build());
    logsTable.insert(LogsRow.defaultRow().taskName("new").time(now).build());

    List<LogModel> logs =
        logLogic.getLogsDirectlyFromDB(
            TaskDetailsRequestParams.builder()
                .startTime(now.minus(Duration.ofHours(1)))
                .endTime(now.minus(Duration.ofMinutes(30)))
                .build());

    assertThat(logs).extracting(LogModel::getTaskName).containsExactly("inside");
  }

  // --- Search ---

  @Test
  void getLogsDirectlyFromDB_searchByTaskName_returnsPartialMatches() {
    logsTable.insert(LogsRow.defaultRow().taskName("send-email").build());
    logsTable.insert(LogsRow.defaultRow().taskName("send-sms").build());
    logsTable.insert(LogsRow.defaultRow().taskName("cleanup").build());

    List<LogModel> logs =
        logLogic.getLogsDirectlyFromDB(
            TaskDetailsRequestParams.builder().searchTermTaskName("send").build());

    assertThat(logs)
        .extracting(LogModel::getTaskName)
        .containsExactlyInAnyOrder("send-email", "send-sms");
  }

  @Test
  void getLogsDirectlyFromDB_searchByTaskNameExactMatch_returnsOnlyExactMatch() {
    logsTable.insert(LogsRow.defaultRow().taskName("send-email").build());
    logsTable.insert(LogsRow.defaultRow().taskName("send-sms").build());

    List<LogModel> logs =
        logLogic.getLogsDirectlyFromDB(
            TaskDetailsRequestParams.builder()
                .searchTermTaskName("send-email")
                .taskNameExactMatch(true)
                .build());

    assertThat(logs).extracting(LogModel::getTaskName).containsExactly("send-email");
  }

  @Test
  void getLogsDirectlyFromDB_searchByTaskInstance_returnsPartialMatches() {
    logsTable.insert(LogsRow.defaultRow().taskInstance("user-42-payment").build());
    logsTable.insert(LogsRow.defaultRow().taskInstance("user-99-payment").build());
    logsTable.insert(LogsRow.defaultRow().taskInstance("system-cleanup").build());

    List<LogModel> logs =
        logLogic.getLogsDirectlyFromDB(
            TaskDetailsRequestParams.builder().searchTermTaskInstance("user").build());

    assertThat(logs)
        .extracting(LogModel::getTaskInstance)
        .containsExactlyInAnyOrder("user-42-payment", "user-99-payment");
  }

  @Test
  void getLogsDirectlyFromDB_searchByTaskInstanceExactMatch_returnsOnlyExactMatch() {
    logsTable.insert(LogsRow.defaultRow().taskInstance("user-42-payment").build());
    logsTable.insert(LogsRow.defaultRow().taskInstance("user-99-payment").build());

    List<LogModel> logs =
        logLogic.getLogsDirectlyFromDB(
            TaskDetailsRequestParams.builder()
                .searchTermTaskInstance("user-42-payment")
                .taskInstanceExactMatch(true)
                .build());

    assertThat(logs).extracting(LogModel::getTaskInstance).containsExactly("user-42-payment");
  }

  // --- Pagination ---

  @Test
  void getLogs_firstPage_returnsPageSliceAndTotalCount() {
    for (int i = 0; i < 5; i++) {
      logsTable.insert(LogsRow.defaultRow().build());
    }

    GetLogsResponse response = logLogic.getLogs(TaskDetailsRequestParams.builder().size(2).build());

    assertThat(response.getNumberOfItems()).isEqualTo(5);
    assertThat(response.getItems()).hasSize(2);
    assertThat(response.getNumberOfPages()).isEqualTo(3);
  }

  @Test
  void getLogs_lastPage_returnsRemainingItems() {
    for (int i = 0; i < 5; i++) {
      logsTable.insert(LogsRow.defaultRow().build());
    }

    GetLogsResponse response =
        logLogic.getLogs(TaskDetailsRequestParams.builder().size(2).pageNumber(2).build());

    assertThat(response.getItems()).hasSize(1);
  }

  @Test
  void getLogs_pageOutOfBounds_returnsEmptyItems() {
    for (int i = 0; i < 3; i++) {
      logsTable.insert(LogsRow.defaultRow().build());
    }

    GetLogsResponse response =
        logLogic.getLogs(TaskDetailsRequestParams.builder().size(2).pageNumber(5).build());

    assertThat(response.getItems()).isEmpty();
    assertThat(response.getNumberOfItems()).isEqualTo(3);
  }

  @Test
  void getLogs_emptyDatabase_returnsZeroItemsAndZeroPages() {
    GetLogsResponse response = logLogic.getLogs(defaultRequest());

    assertThat(response.getNumberOfItems()).isZero();
    assertThat(response.getItems()).isEmpty();
    assertThat(response.getNumberOfPages()).isZero();
  }

  // --- Polling ---

  @Test
  void pollLogs_emptyCache_countsAllLogsAsNew() {
    logsTable.insert(LogsRow.defaultRow().taskName("ok-job").succeeded(true).build());
    logsTable.insert(LogsRow.defaultRow().taskName("failing-job-1").succeeded(false).build());
    logsTable.insert(LogsRow.defaultRow().taskName("failing-job-2").succeeded(false).build());

    LogPollResponse response = logLogic.pollLogs(defaultRequest());

    assertThat(response.getNewSucceeded()).isEqualTo(1);
    assertThat(response.getNewFailures()).isEqualTo(2);
  }

  @Test
  void pollLogs_logsAlreadyInCache_reportsNoNewLogs() {
    logsTable.insert(LogsRow.defaultRow().succeeded(true).build());

    caching.updateLogCache(logLogic.getLogsDirectlyFromDB(defaultRequest()));

    LogPollResponse response = logLogic.pollLogs(defaultRequest());

    assertThat(response.getNewSucceeded()).isZero();
    assertThat(response.getNewFailures()).isZero();
  }

  @Test
  void pollLogs_mixedCachedAndNewLogs_countsOnlyNew() {
    logsTable.insert(LogsRow.defaultRow().taskName("cached").succeeded(true).build());

    caching.updateLogCache(logLogic.getLogsDirectlyFromDB(defaultRequest()));

    logsTable.insert(LogsRow.defaultRow().taskName("new-success").succeeded(true).build());
    logsTable.insert(LogsRow.defaultRow().taskName("new-failure").succeeded(false).build());

    LogPollResponse response = logLogic.pollLogs(defaultRequest());

    assertThat(response.getNewSucceeded()).isEqualTo(1);
    assertThat(response.getNewFailures()).isEqualTo(1);
  }

  // --- Helper ---

  private static TaskDetailsRequestParams defaultRequest() {
    return TaskDetailsRequestParams.builder().build();
  }
}
