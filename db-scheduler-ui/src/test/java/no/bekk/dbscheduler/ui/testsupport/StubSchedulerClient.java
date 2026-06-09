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
package no.bekk.dbscheduler.ui.testsupport;

import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.ScheduledExecutionsFilter;
import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.TaskSummary;
import com.github.kagkarlsson.scheduler.task.SchedulableInstance;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.TaskInstanceId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Minimal {@link SchedulerClient} test double that only answers {@link
 * #getScheduledExecutionsSummaryByTask()} — the single method {@code OverviewLogic} depends on. All
 * other operations throw, so a test that accidentally relies on them fails loudly rather than
 * silently observing no-op behaviour.
 */
public final class StubSchedulerClient implements SchedulerClient {

  private final List<TaskSummary> summaries;

  public StubSchedulerClient(List<TaskSummary> summaries) {
    this.summaries = summaries;
  }

  @Override
  public List<TaskSummary> getScheduledExecutionsSummaryByTask() {
    return summaries;
  }

  @Override
  public <T> boolean schedule(TaskInstance<T> taskInstance, Instant time, ScheduleOptions options) {
    throw unsupported();
  }

  @Override
  public <T> boolean schedule(SchedulableInstance<T> instance, ScheduleOptions options) {
    throw unsupported();
  }

  @Override
  public <T> void schedule(TaskInstance<T> taskInstance, Instant time) {
    throw unsupported();
  }

  @Override
  public <T> void schedule(SchedulableInstance<T> instance) {
    throw unsupported();
  }

  @Override
  public <T> boolean scheduleIfNotExists(TaskInstance<T> taskInstance, Instant time) {
    throw unsupported();
  }

  @Override
  public <T> boolean scheduleIfNotExists(SchedulableInstance<T> instance) {
    throw unsupported();
  }

  @Override
  public void scheduleBatch(List<TaskInstance<?>> taskInstances, Instant time) {
    throw unsupported();
  }

  @Override
  public void scheduleBatch(List<SchedulableInstance<?>> instances) {
    throw unsupported();
  }

  @Override
  public boolean reschedule(TaskInstanceId taskInstanceId, Instant newExecutionTime) {
    throw unsupported();
  }

  @Override
  public <T> boolean reschedule(
      TaskInstanceId taskInstanceId, Instant newExecutionTime, T newData) {
    throw unsupported();
  }

  @Override
  public <T> boolean reschedule(SchedulableInstance<T> instance) {
    throw unsupported();
  }

  @Override
  public void cancel(TaskInstanceId taskInstanceId) {
    throw unsupported();
  }

  @Override
  public void fetchScheduledExecutions(Consumer<ScheduledExecution<Object>> consumer) {
    throw unsupported();
  }

  @Override
  public void fetchScheduledExecutions(
      ScheduledExecutionsFilter filter, Consumer<ScheduledExecution<Object>> consumer) {
    throw unsupported();
  }

  @Override
  public <T> void fetchScheduledExecutionsForTask(
      String taskName, Class<T> dataClass, Consumer<ScheduledExecution<T>> consumer) {
    throw unsupported();
  }

  @Override
  public <T> void fetchScheduledExecutionsForTask(
      String taskName,
      Class<T> dataClass,
      ScheduledExecutionsFilter filter,
      Consumer<ScheduledExecution<T>> consumer) {
    throw unsupported();
  }

  @Override
  public Optional<ScheduledExecution<Object>> getScheduledExecution(TaskInstanceId taskInstanceId) {
    throw unsupported();
  }

  private static UnsupportedOperationException unsupported() {
    return new UnsupportedOperationException("Not supported by StubSchedulerClient");
  }
}
