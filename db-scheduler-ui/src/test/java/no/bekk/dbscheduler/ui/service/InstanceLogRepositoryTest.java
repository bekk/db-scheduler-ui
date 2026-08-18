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
import java.util.Comparator;
import java.util.List;
import javax.sql.DataSource;
import no.bekk.dbscheduler.ui.model.InstanceHistory.InstanceRun;
import no.bekk.dbscheduler.ui.testsupport.LogsRow;
import no.bekk.dbscheduler.ui.testsupport.LogsTable;
import no.bekk.dbscheduler.ui.testsupport.TestDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InstanceLogRepositoryTest {

  private static final Instant NOW = Instant.parse("2026-08-07T10:00:00Z");

  private LogsTable logsTable;
  private InstanceLogRepository repository;

  @BeforeEach
  void setUp() {
    DataSource dataSource = TestDatabase.newDataSourceWithTables();
    logsTable = new LogsTable(dataSource);
    repository = new InstanceLogRepository(dataSource, LogsTable.NAME);
  }

  @Test
  void findRecentRuns_returnsNewestFirst() {
    insert("webhook", "evt-1", true, NOW.minus(Duration.ofMinutes(30)));
    insert("webhook", "evt-1", false, NOW.minus(Duration.ofMinutes(20)));
    insert("webhook", "evt-1", true, NOW.minus(Duration.ofMinutes(10)));

    List<InstanceRun> runs = repository.findRecentRuns("webhook", "evt-1", 10);

    assertThat(runs).hasSize(3);
    assertThat(runs)
        .extracting(InstanceRun::id)
        .isSortedAccordingTo(Comparator.<Long>reverseOrder());
    assertThat(runs.get(0).timeStarted()).isEqualTo(NOW.minus(Duration.ofMinutes(10)));
  }

  @Test
  void findRecentRuns_honoursTheLimit() {
    for (int i = 0; i < 8; i++) {
      insert("webhook", "evt-1", true, NOW.minusSeconds(i));
    }

    assertThat(repository.findRecentRuns("webhook", "evt-1", 3)).hasSize(3);
  }

  @Test
  void findRecentRuns_isScopedToOneInstanceOfOneTask() {
    insert("webhook", "evt-1", true, NOW);
    insert("webhook", "evt-2", true, NOW);
    insert("other-task", "evt-1", true, NOW);

    List<InstanceRun> runs = repository.findRecentRuns("webhook", "evt-1", 10);

    assertThat(runs).hasSize(1);
  }

  @Test
  void findRecentRuns_omitsStackTraces() {
    logsTable.insert(
        LogsRow.failedRow().taskName("webhook").taskInstance("evt-1").time(NOW).build());

    List<InstanceRun> runs = repository.findRecentRuns("webhook", "evt-1", 10);

    // The list only shows outcome and message; a trace per row would dominate the payload.
    assertThat(runs).singleElement().satisfies(run -> assertThat(run.stackTrace()).isNull());
    assertThat(runs.get(0).exceptionMessage()).isEqualTo("boom");
  }

  @Test
  void findLastFailure_returnsMostRecentFailureWithItsTrace() {
    logsTable.insert(
        LogsRow.failedRow()
            .taskName("webhook")
            .taskInstance("evt-1")
            .exceptionMessage("older")
            .time(NOW.minus(Duration.ofMinutes(10)))
            .build());
    logsTable.insert(
        LogsRow.failedRow()
            .taskName("webhook")
            .taskInstance("evt-1")
            .exceptionMessage("newer")
            .time(NOW)
            .build());
    insert("webhook", "evt-1", true, NOW.plusSeconds(30));

    InstanceRun failure = repository.findLastFailure("webhook", "evt-1").orElseThrow();

    assertThat(failure.exceptionMessage()).isEqualTo("newer");
    assertThat(failure.exceptionClass()).isEqualTo("java.lang.RuntimeException");
    assertThat(failure.stackTrace()).contains("com.acme.Worker.run(Worker.java:42)");
    assertThat(failure.succeeded()).isFalse();
  }

  @Test
  void findLastFailure_neverFailed_isEmpty() {
    insert("webhook", "evt-1", true, NOW);

    assertThat(repository.findLastFailure("webhook", "evt-1")).isEmpty();
  }

  private void insert(String taskName, String instanceId, boolean succeeded, Instant time) {
    logsTable.insert(
        LogsRow.defaultRow()
            .taskName(taskName)
            .taskInstance(instanceId)
            .succeeded(succeeded)
            .time(time)
            .build());
  }
}
