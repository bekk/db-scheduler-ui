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

import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.task.Execution;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import no.bekk.dbscheduler.ui.model.InstanceDetail;
import no.bekk.dbscheduler.ui.model.InstanceStatus;
import no.bekk.dbscheduler.ui.service.InstanceService.SoleInstance;
import no.bekk.dbscheduler.ui.testsupport.StubSchedulerClient;
import org.junit.jupiter.api.Test;

class InstanceServiceTest {

  private static final Instant NOW = Instant.parse("2026-08-07T10:00:00Z");

  @Test
  void getInstance_mapsTheExecution() {
    InstanceService service =
        service(execution("webhook", "evt-1", NOW.plusSeconds(60), false, null, 3));

    InstanceDetail detail = service.getInstance("webhook", "evt-1").orElseThrow();

    assertThat(detail.taskName()).isEqualTo("webhook");
    assertThat(detail.id()).isEqualTo("evt-1");
    assertThat(detail.status()).isEqualTo(InstanceStatus.FAILED);
    assertThat(detail.executionTime()).isEqualTo(NOW.plusSeconds(60));
    assertThat(detail.consecutiveFailures()).isEqualTo(3);
    assertThat(detail.picked()).isFalse();
    // No log repository was supplied, which is what history=false looks like to this service.
    assertThat(detail.history()).isNull();
  }

  @Test
  void getInstance_unknownInstance_isEmpty() {
    InstanceService service = service(execution("webhook", "evt-1", NOW, false, null, 0));

    assertThat(service.getInstance("webhook", "nope")).isEmpty();
    assertThat(service.getInstance("other-task", "evt-1")).isEmpty();
  }

  @Test
  void status_pickedWins_overConsecutiveFailures() {
    InstanceService service = service(execution("webhook", "evt-1", NOW, true, "scheduler-2", 4));

    InstanceDetail detail = service.getInstance("webhook", "evt-1").orElseThrow();

    assertThat(detail.status()).isEqualTo(InstanceStatus.RUNNING);
    assertThat(detail.pickedBy()).isEqualTo("scheduler-2");
  }

  @Test
  void status_noFailures_isScheduled() {
    InstanceService service = service(execution("webhook", "evt-1", NOW, false, null, 0));

    assertThat(service.getInstance("webhook", "evt-1").orElseThrow().status())
        .isEqualTo(InstanceStatus.SCHEDULED);
  }

  @Test
  void taskData_isWithheldWhenNotEnabled() {
    ScheduledExecution<Object> execution =
        execution("webhook", "evt-1", NOW, false, null, 0, "the-payload");

    assertThat(
            new InstanceService(new StubSchedulerClient(List.of(), List.of(execution)), null, true)
                .getInstance("webhook", "evt-1")
                .orElseThrow()
                .taskData())
        .isEqualTo("the-payload");
    assertThat(
            new InstanceService(new StubSchedulerClient(List.of(), List.of(execution)), null, false)
                .getInstance("webhook", "evt-1")
                .orElseThrow()
                .taskData())
        .isNull();
  }

  @Test
  void getSoleInstance_oneExecution_resolvesIt() {
    InstanceService service = service(execution("webhook", "evt-1", NOW, false, null, 0));

    SoleInstance sole = service.getSoleInstance("webhook");

    assertThat(sole.executionCount()).isEqualTo(1);
    assertThat(sole.detail()).isNotNull();
    assertThat(sole.detail().id()).isEqualTo("evt-1");
  }

  @Test
  void getSoleInstance_severalExecutions_refusesToGuess() {
    InstanceService service =
        service(
            execution("webhook", "evt-1", NOW, false, null, 0),
            execution("webhook", "evt-2", NOW.plus(Duration.ofMinutes(1)), false, null, 0));

    SoleInstance sole = service.getSoleInstance("webhook");

    assertThat(sole.executionCount()).isEqualTo(2);
    assertThat(sole.detail()).isNull();
  }

  @Test
  void getSoleInstance_findsARunningExecution() {
    // The convenience lookup on SchedulerClient filters to picked=false, hiding a running
    // execution.
    InstanceService service = service(execution("webhook", "evt-1", NOW, true, "scheduler-1", 0));

    SoleInstance sole = service.getSoleInstance("webhook");

    assertThat(sole.executionCount()).isEqualTo(1);
    assertThat(sole.detail()).isNotNull();
    assertThat(sole.detail().status()).isEqualTo(InstanceStatus.RUNNING);
  }

  @Test
  void getSoleInstance_noExecutions_reportsZero() {
    SoleInstance sole =
        service(execution("other", "evt-1", NOW, false, null, 0)).getSoleInstance("webhook");

    assertThat(sole.executionCount()).isZero();
    assertThat(sole.detail()).isNull();
  }

  @SafeVarargs
  private static InstanceService service(ScheduledExecution<Object>... executions) {
    // null log repository is what history=false looks like to this service.
    return new InstanceService(new StubSchedulerClient(List.of(), List.of(executions)), null, true);
  }

  private static ScheduledExecution<Object> execution(
      String taskName,
      String id,
      Instant executionTime,
      boolean picked,
      String pickedBy,
      int consecutiveFailures) {
    return execution(taskName, id, executionTime, picked, pickedBy, consecutiveFailures, null);
  }

  private static ScheduledExecution<Object> execution(
      String taskName,
      String id,
      Instant executionTime,
      boolean picked,
      String pickedBy,
      int consecutiveFailures,
      Object data) {
    return new ScheduledExecution<>(
        Object.class,
        new Execution(
            executionTime,
            new TaskInstance<>(taskName, id, data),
            picked,
            pickedBy,
            NOW.minus(Duration.ofHours(1)),
            NOW.minus(Duration.ofMinutes(30)),
            consecutiveFailures,
            NOW,
            1L));
  }
}
