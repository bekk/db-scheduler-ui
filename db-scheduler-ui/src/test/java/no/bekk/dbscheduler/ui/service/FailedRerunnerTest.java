package no.bekk.dbscheduler.ui.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import javax.sql.DataSource;
import no.bekk.dbscheduler.ui.testsupport.ScheduledTasksTable;
import no.bekk.dbscheduler.ui.testsupport.TestDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class FailedRerunnerTest {

  private static final Instant NOW = Instant.parse("2026-11-21T10:00:00Z");
  private static final Instant CUTOFF = NOW;
  private static final String TASK = "invoice";

  private final OneTimeTask<Void> invoice = Tasks.oneTime(TASK).execute((instance, context) -> {});

  private DataSource dataSource;
  private SchedulerClient client;

  @BeforeEach
  void setUp() {
    dataSource = TestDatabase.newDataSourceWithTables();
    ScheduledTasksTable.createSchema(dataSource);
    client = SchedulerClient.Builder.create(dataSource, List.of(invoice)).build();
  }

  @Test
  void rerun_onlyFailingAndUnpicked() {
    failing("failing-before", NOW.minus(Duration.ofHours(1)));
    schedule("scheduled-before", NOW.minus(Duration.ofHours(1)));
    failing("picked-failing", NOW.minus(Duration.ofHours(1)));
    ScheduledTasksTable.markPicked(dataSource, TASK, "picked-failing");

    int rescheduled = rerunner(client, 3).rerunFailed(TASK, CUTOFF);

    assertThat(rescheduled).isEqualTo(1);
    assertThat(executionTime("failing-before")).isEqualTo(NOW);
    assertThat(executionTime("scheduled-before")).isEqualTo(NOW.minus(Duration.ofHours(1)));
    assertThat(executionTime("picked-failing")).isEqualTo(NOW.minus(Duration.ofHours(1)));
  }

  @Test
  void rerun_includesFailuresAwaitingALaterRetry() {
    // A failure handler typically moves the execution to a later retry.
    failing("retry-later", NOW.plus(Duration.ofMinutes(30)));

    assertThat(rerunner(client, 3).rerunFailed(TASK, CUTOFF)).isEqualTo(1);
    assertThat(executionTime("retry-later")).isEqualTo(NOW);
  }

  @Test
  void rerun_clientClockAhead_cutoffIsClampedToServerNow() {
    // The browser sends its own "now"; an hour fast, it would otherwise reach failures that
    // happened after the server started this rerun.
    failing("failed-after-server-now", NOW.minus(Duration.ofHours(1)));
    setLastFailure("failed-after-server-now", NOW.plus(Duration.ofMinutes(10)));

    assertThat(rerunner(client, 3).rerunFailed(TASK, NOW.plus(Duration.ofHours(1)))).isZero();
  }

  @Test
  void rerun_skipsFailuresAfterTheCutoff() {
    failing("failed-after-click", NOW.minus(Duration.ofHours(1)));
    setLastFailure("failed-after-click", CUTOFF.plusSeconds(5));

    assertThat(rerunner(client, 3).rerunFailed(TASK, CUTOFF)).isZero();
  }

  @Test
  void rerun_manyAcrossBatches_eachCountedOnce() {
    for (int i = 0; i < 8; i++) {
      failing("past-" + i, NOW.minus(Duration.ofMinutes(10 + i)));
      failing("future-" + i, NOW.plus(Duration.ofMinutes(10 + i)));
    }

    assertThat(rerunner(client, 3).rerunFailed(TASK, CUTOFF)).isEqualTo(16);
  }

  @Test
  void rerun_walksAllBatches() {
    for (int i = 0; i < 8; i++) {
      failing("f-" + i, NOW.minus(Duration.ofMinutes(10 + i)));
    }

    assertThat(rerunner(client, 3).rerunFailed(TASK, CUTOFF)).isEqualTo(8);
  }

  @Test
  void rerun_twice_secondRunFindsNothing() {
    failing("a", NOW.minus(Duration.ofHours(1)));
    failing("b", NOW.minus(Duration.ofHours(2)));

    assertThat(rerunner(client, 3).rerunFailed(TASK, CUTOFF)).isEqualTo(2);
    assertThat(rerunner(client, 3).rerunFailed(TASK, CUTOFF)).isZero();
  }

  @Test
  void rerun_skipsRowsPickedMeanwhile() {
    failing("a", NOW.minus(Duration.ofHours(2)));
    failing("b", NOW.minus(Duration.ofHours(1)));

    // Marks "a" picked after the rerunner has read it, as a scheduler node would.
    SchedulerClient racing =
        (SchedulerClient)
            Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] {SchedulerClient.class},
                (proxy, method, args) -> {
                  Object result;
                  try {
                    result = method.invoke(client, args);
                  } catch (InvocationTargetException e) {
                    throw e.getCause();
                  }
                  if (method.getName().equals("getScheduledExecutionsForTask")) {
                    ScheduledTasksTable.markPicked(dataSource, TASK, "a");
                  }
                  return result;
                });

    assertThat(rerunner(racing, 3).rerunFailed(TASK, CUTOFF)).isEqualTo(1);
    assertThat(executionTime("a")).isEqualTo(NOW.minus(Duration.ofHours(2)));
  }

  private void schedule(String id, Instant time) {
    client.scheduleIfNotExists(invoice.instance(id), time);
  }

  private void failing(String id, Instant time) {
    schedule(id, time);
    ScheduledTasksTable.markFailing(dataSource, TASK, id, 2);
    setLastFailure(id, CUTOFF.minus(Duration.ofMinutes(1)));
  }

  private void setLastFailure(String id, Instant lastFailure) {
    new JdbcTemplate(dataSource)
        .update(
            "update scheduled_tasks set last_failure = ? where task_name = ? and task_instance = ?",
            java.sql.Timestamp.from(lastFailure),
            TASK,
            id);
  }

  private Instant executionTime(String id) {
    return new JdbcTemplate(dataSource)
        .queryForObject(
            "select execution_time from scheduled_tasks where task_name = ? and task_instance = ?",
            java.sql.Timestamp.class,
            TASK,
            id)
        .toInstant();
  }

  private static FailedRerunner rerunner(SchedulerClient client, int batchSize) {
    return new FailedRerunner(client, Clock.fixed(NOW, ZoneOffset.UTC), batchSize);
  }
}
