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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class TaskModel {
  private String taskName;
  private List<String> taskInstance;
  private List<Object> taskData;
  private List<Instant> executionTime;
  private List<Boolean> picked;
  private List<String> pickedBy;
  private List<Instant> lastSuccess;
  private Instant lastFailure;
  private List<Integer> consecutiveFailures;
  private Instant lastHeartbeat;
  private int version;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public TaskModel(
      String taskName,
      List<String> taskInstance,
      List<Object> inputTaskData,
      List<Instant> executionTime,
      List<Boolean> picked,
      List<String> pickedBy,
      List<Instant> lastSuccess,
      Instant lastFailure,
      List<Integer> consecutiveFailures,
      Instant lastHeartbeat,
      int version) {
    this.taskName = taskName;
    this.taskInstance = taskInstance;
    this.taskData = serializeTaskData(inputTaskData);
    this.executionTime = executionTime;
    this.picked = picked;
    this.pickedBy = pickedBy;
    this.lastSuccess = lastSuccess;
    this.lastFailure = lastFailure;
    this.consecutiveFailures = consecutiveFailures;
    this.lastHeartbeat = lastHeartbeat;
    this.version = version;
  }

  public TaskModel() {}

  private List<Object> serializeTaskData(List<Object> inputTaskDataList) {
    return inputTaskDataList.stream()
        .map(
            data -> {
              try {
                String serializedData = objectMapper.writeValueAsString(data);
                return objectMapper.readValue(serializedData, Object.class);
              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              }
            })
        .collect(Collectors.toList());
  }

  public String getTaskName() {
    return taskName;
  }

  public List<String> getTaskInstance() {
    return taskInstance;
  }

  public void setTaskInstance(List<String> taskInstance) {
    this.taskInstance = taskInstance;
  }

  public List<Object> getTaskData() {
    return taskData;
  }

  public void setTaskData(List<Object> inputTaskData) {
    this.taskData = serializeTaskData(inputTaskData);
  }

  public void setExecutionTime(List<Instant> executionTime) {
    this.executionTime = executionTime;
  }

  public List<Instant> getExecutionTime() {
    return executionTime;
  }

  public List<Boolean> isPicked() {
    return picked;
  }

  public void setPicked(List<Boolean> picked) {
    this.picked = picked;
  }

  public List<String> getPickedBy() {
    return pickedBy;
  }

  public void setPickedBy(List<String> pickedBy) {
    this.pickedBy = pickedBy;
  }

  public List<Instant> getLastSuccess() {
    return lastSuccess;
  }

  public void setLastSuccess(List<Instant> lastSuccess) {
    this.lastSuccess = lastSuccess;
  }

  public Instant getLastFailure() {
    return lastFailure;
  }

  public void setLastFailure(Instant lastFailure) {
    this.lastFailure = lastFailure;
  }

  public List<Integer> getConsecutiveFailures() {
    return consecutiveFailures;
  }

  public Instant getLastHeartbeat() {
    return lastHeartbeat;
  }

  public void setLastHeartbeat(Instant lastHeartbeat) {
    this.lastHeartbeat = lastHeartbeat;
  }

  public int getVersion() {
    return version;
  }

  public void setConsecutiveFailures(List<Integer> consecutiveFailures) {
    this.consecutiveFailures = consecutiveFailures;
  }
}
