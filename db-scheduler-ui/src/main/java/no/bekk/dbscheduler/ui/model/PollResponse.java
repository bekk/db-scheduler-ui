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
package no.bekk.dbscheduler.ui.model;

public class PollResponse {
  private final int newFailures;
  private final int newRunning;
  private final int newTasks;
  private final int stoppedFailing;
  private final int finishedRunning;

  public PollResponse(
      int newFailures, int newRunning, int newTasks, int stoppedFailing, int finishedRunning) {
    this.newFailures = newFailures;
    this.newRunning = newRunning;
    this.newTasks = newTasks;
    this.stoppedFailing = stoppedFailing;
    this.finishedRunning = finishedRunning;
  }

  public int getNewFailures() {
    return newFailures;
  }

  public int getNewRunning() {
    return newRunning;
  }

  public int getNewTasks() {
    return newTasks;
  }

  public int getStoppedFailing() {
    return stoppedFailing;
  }

  public int getFinishedRunning() {
    return finishedRunning;
  }
}
