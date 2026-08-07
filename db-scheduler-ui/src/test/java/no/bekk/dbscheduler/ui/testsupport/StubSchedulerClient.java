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
 * Minimal {@link SchedulerClient} test double answering only the lookups the UI services depend on:
 * {@link #getScheduledExecutionsSummaryByTask()} for the overview, and the by-id / by-task
 * execution lookups for the instance detail. All other operations throw, so a test that
 * accidentally relies on them fails loudly rather than silently observing no-op behaviour.
 */
public final class StubSchedulerClient implements SchedulerClient {

  private final List<TaskSummary> summaries;
  private final List<ScheduledExecution<Object>> executions;

  public StubSchedulerClient(List<TaskSummary> summaries) {
    this(summaries, List.of());
  }

  public StubSchedulerClient(
      List<TaskSummary> summaries, List<ScheduledExecution<Object>> executions) {
    this.summaries = summaries;
    this.executions = executions;
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
    // Mirrors the real client, which quietly narrows this overload to unpicked executions
    // (SchedulerClient:619) — modelled so that a caller relying on it fails here too.
    fetchScheduledExecutionsForTask(
        taskName, dataClass, ScheduledExecutionsFilter.all().withPicked(false), consumer);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> void fetchScheduledExecutionsForTask(
      String taskName,
      Class<T> dataClass,
      ScheduledExecutionsFilter filter,
      Consumer<ScheduledExecution<T>> consumer) {
    executions.stream()
        .filter(execution -> execution.getTaskInstance().getTaskName().equals(taskName))
        .filter(
            execution ->
                filter.getPickedValue().map(picked -> picked == execution.isPicked()).orElse(true))
        .limit(filter.getLimit().orElse(Integer.MAX_VALUE))
        .forEach(execution -> consumer.accept((ScheduledExecution<T>) execution));
  }

  @Override
  public Optional<ScheduledExecution<Object>> getScheduledExecution(TaskInstanceId taskInstanceId) {
    return executions.stream()
        .filter(
            execution ->
                execution.getTaskInstance().getTaskName().equals(taskInstanceId.getTaskName())
                    && execution.getTaskInstance().getId().equals(taskInstanceId.getId()))
        .findFirst();
  }

  private static UnsupportedOperationException unsupported() {
    return new UnsupportedOperationException("Not supported by StubSchedulerClient");
  }
}
