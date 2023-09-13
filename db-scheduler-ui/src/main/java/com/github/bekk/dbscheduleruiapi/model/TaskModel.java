package com.github.bekk.dbscheduleruiapi.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TaskModel {
  private String taskName;
  private List<String> taskInstance;
  private List<String> taskData; // Serialized JSON representation of data
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
      List<Object> taskData,
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
    serializeTaskData(taskData);
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

  public String getTaskName() {
    return taskName;
  }

  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }

  public List<String> getTaskInstance() {
    return taskInstance;
  }

  public void setTaskInstance(List<String> taskInstance) {
    this.taskInstance = taskInstance;
  }

  public Object getActualTaskData() {
    return taskData.stream()
        .map(
            data -> {
              if (data != null) {
                try {
                  return objectMapper.readValue(data, Object.class);
                } catch (JsonProcessingException e) {
                  throw new RuntimeException(e);
                }
              }
              return null;
            })
        .collect(Collectors.toList());
  }

  public List<String> getTaskData() {
    return this.taskData;
  }

  public void serializeTaskData(List<Object> taskData) {
    try {
      assert taskData != null;
      this.taskData = Arrays.asList(objectMapper.writeValueAsString(taskData.get(0)));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  public void setTaskData(List<String> taskData) {
    this.taskData = taskData;
  }

  public List<Instant> getExecutionTime() {
    return executionTime;
  }

  public void setExecutionTime(List<Instant> executionTime) {
    this.executionTime = executionTime;
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

  public void setConsecutiveFailures(List<Integer> consecutiveFailures) {
    this.consecutiveFailures = consecutiveFailures;
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

  public void setVersion(int version) {
    this.version = version;
  }
}
