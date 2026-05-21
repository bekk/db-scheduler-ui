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
package no.bekk.dbscheduler.ui.log;

import com.github.kagkarlsson.scheduler.task.ExecutionComplete;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import java.time.Instant;
import java.util.Objects;

public final class ExecutionLog {

  public final TaskInstance<?> taskInstance;
  public final String pickedBy;
  public final Instant timeStarted;
  public final Instant timeFinished;
  public final boolean succeeded;
  public final Throwable cause;

  public ExecutionLog(ExecutionComplete exec) {
    taskInstance = exec.getExecution().taskInstance;
    pickedBy = exec.getExecution().pickedBy;
    timeStarted = exec.getTimeDone().minus(exec.getDuration());
    timeFinished = exec.getTimeDone();
    succeeded = ExecutionComplete.Result.OK.equals(exec.getResult());
    cause = exec.getCause().orElse(null);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExecutionLog execLog = (ExecutionLog) o;
    return Objects.equals(timeStarted, execLog.timeStarted)
        && Objects.equals(timeFinished, execLog.timeFinished)
        && Objects.equals(taskInstance, execLog.taskInstance);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timeStarted, timeFinished, taskInstance);
  }

  @Override
  public String toString() {
    return "ExecutionLog: "
        + "task="
        + taskInstance.getTaskName()
        + ", id="
        + taskInstance.getId()
        + ", pickedBy="
        + pickedBy
        + ", timeStarted="
        + timeStarted
        + ", timeFinished="
        + timeFinished
        + ", succeeded="
        + succeeded;
  }
}
