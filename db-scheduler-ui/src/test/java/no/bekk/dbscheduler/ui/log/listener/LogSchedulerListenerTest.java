package no.bekk.dbscheduler.ui.log.listener;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.kagkarlsson.scheduler.task.Execution;
import com.github.kagkarlsson.scheduler.task.ExecutionComplete;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import no.bekk.dbscheduler.ui.log.ExecutionLog;
import no.bekk.dbscheduler.ui.log.LogRepository;
import org.junit.jupiter.api.Test;

class LogSchedulerListenerTest {

  @Test
  void forwardsCompletedExecutionToRepository() throws Exception {
    List<ExecutionLog> captured = new ArrayList<>();
    LogRepository repo =
        log -> {
          captured.add(log);
          return true;
        };

    ExecutorService synchronous =
        Executors.newSingleThreadExecutor(); // sequential so we can join on it
    try (LogSchedulerListener listener = new LogSchedulerListener(repo, synchronous)) {
      Instant started = Instant.parse("2026-01-01T12:00:00Z");
      ExecutionComplete event =
          ExecutionComplete.success(
              new Execution(started, new TaskInstance<>("task-a", "instance-1")),
              started,
              started.plusSeconds(1));

      listener.onExecutionComplete(event);

      synchronous.shutdown();
      assertThat(synchronous.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
    }

    assertThat(captured).singleElement().satisfies(log -> {
      assertThat(log.taskInstance().getTaskName()).isEqualTo("task-a");
      assertThat(log.succeeded()).isTrue();
    });
  }
}
