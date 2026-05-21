/*
 * Copyright (C) Marten Prieß
 * Originally part of https://github.com/rocketbase-io/db-scheduler-log
 * Copyright (C) Bekk (modifications)
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
package no.bekk.dbscheduler.ui.log.listener;

import com.github.kagkarlsson.scheduler.event.AbstractSchedulerListener;
import com.github.kagkarlsson.scheduler.task.ExecutionComplete;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import no.bekk.dbscheduler.ui.log.ExecutionLog;
import no.bekk.dbscheduler.ui.log.LogRepository;

public class LogSchedulerListener extends AbstractSchedulerListener implements AutoCloseable {

  private final LogRepository logRepository;
  private final ExecutorService executorService;

  public LogSchedulerListener(LogRepository logRepository) {
    this(logRepository, Executors.newFixedThreadPool(5));
  }

  public LogSchedulerListener(LogRepository logRepository, ExecutorService executorService) {
    this.logRepository = logRepository;
    this.executorService = executorService;
  }

  @Override
  public void onExecutionComplete(ExecutionComplete executionComplete) {
    ExecutionLog log = new ExecutionLog(executionComplete);
    executorService.submit(() -> logRepository.createIfNotExists(log));
  }

  @Override
  public void close() {
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
