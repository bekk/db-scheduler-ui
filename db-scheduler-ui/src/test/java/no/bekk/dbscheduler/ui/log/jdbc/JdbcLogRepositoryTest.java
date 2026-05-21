package no.bekk.dbscheduler.ui.log.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.kagkarlsson.scheduler.serializer.JavaSerializer;
import com.github.kagkarlsson.scheduler.task.Execution;
import com.github.kagkarlsson.scheduler.task.ExecutionComplete;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import no.bekk.dbscheduler.ui.log.ExecutionLog;
import no.bekk.dbscheduler.ui.testsupport.LogsTable;
import no.bekk.dbscheduler.ui.testsupport.TestDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class JdbcLogRepositoryTest {

  private DataSource dataSource;
  private JdbcLogRepository repo;
  private JdbcTemplate jdbc;

  @BeforeEach
  void setUp() {
    dataSource = TestDatabase.newDataSourceWithTables();
    jdbc = new JdbcTemplate(dataSource);
    repo = new JdbcLogRepository(dataSource, JavaSerializer::new, LogsTable.NAME, new Snowflake(1));
  }

  @Test
  void insertsSuccessfulExecution() {
    Instant started = Instant.parse("2026-01-01T12:00:00Z");
    Instant finished = started.plusSeconds(2);
    ExecutionComplete event =
        ExecutionComplete.success(
            new Execution(started, new TaskInstance<>("task-a", "instance-1")), started, finished);

    assertThat(repo.createIfNotExists(ExecutionLog.from(event))).isTrue();

    List<Map<String, Object>> rows = jdbc.queryForList("select * from " + LogsTable.NAME);
    assertThat(rows)
        .singleElement()
        .satisfies(
            row -> {
              assertThat(row).containsEntry("task_name", "task-a");
              assertThat(row).containsEntry("task_instance", "instance-1");
              assertThat(row).containsEntry("succeeded", Boolean.TRUE);
              assertThat(row).containsEntry("duration_ms", 2000L);
              assertThat(row.get("exception_class")).isNull();
            });
  }

  @Test
  void insertsFailedExecutionWithStacktrace() {
    Instant started = Instant.parse("2026-01-01T12:00:00Z");
    Instant finished = started.plusMillis(500);
    RuntimeException cause = new RuntimeException("boom");
    ExecutionComplete event =
        ExecutionComplete.failure(
            new Execution(started, new TaskInstance<>("task-b", "instance-2")),
            started,
            finished,
            cause);

    assertThat(repo.createIfNotExists(ExecutionLog.from(event))).isTrue();

    Map<String, Object> row = jdbc.queryForMap("select * from " + LogsTable.NAME);
    assertThat(row).containsEntry("succeeded", Boolean.FALSE);
    assertThat(row).containsEntry("exception_class", "java.lang.RuntimeException");
    assertThat(row).containsEntry("exception_message", "boom");
    assertThat((String) row.get("exception_stacktrace")).contains("boom");
  }

  @Test
  void returnsFalseOnDuplicateId() {
    IdProvider fixedId = () -> 99L;
    JdbcLogRepository fixedRepo =
        new JdbcLogRepository(dataSource, JavaSerializer::new, LogsTable.NAME, fixedId);
    Instant now = Instant.parse("2026-01-01T12:00:00Z");
    ExecutionComplete event =
        ExecutionComplete.success(
            new Execution(now, new TaskInstance<>("task-c", "instance-3")), now, now.plusMillis(1));

    assertThat(fixedRepo.createIfNotExists(ExecutionLog.from(event))).isTrue();
    assertThat(fixedRepo.createIfNotExists(ExecutionLog.from(event))).isFalse();
  }
}
