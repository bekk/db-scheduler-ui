package com.github.bekk.dbscheduleruiapi.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import org.springframework.util.SerializationUtils;

public class LogModel {
  private final Long id;
  private final String taskName;
  private final String taskInstance;
  private final Object taskData;
  private final Instant timeStarted;
  private final Instant timeFinished;
  private final boolean succeeded;
  private final Long durationMs;
  private final String exceptionClass;
  private final String exceptionMessage;
  private final String exceptionStackTrace;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public LogModel(
      Long id,
      String taskName,
      String taskInstance,
      byte[] inputTaskData,
      Instant timeStarted,
      Instant timeFinished,
      boolean succeeded,
      Long durationMs,
      String exceptionClass,
      String exceptionMessage,
      String exceptionStackTrace) {
    this.id = id;
    this.taskName = taskName;
    this.taskInstance = taskInstance;
    this.taskData = stringTaskData(inputTaskData);
    this.timeStarted = timeStarted;
    this.timeFinished = timeFinished;
    this.succeeded = succeeded;
    this.durationMs = durationMs;
    this.exceptionClass = exceptionClass;
    this.exceptionMessage = exceptionMessage;
    this.exceptionStackTrace = exceptionStackTrace;
  }

  private Object stringTaskData(byte[] inputTaskData) {
    try {
      if (inputTaskData != null) {
        Object dataclass = SerializationUtils.deserialize(inputTaskData);
        String serializedData = objectMapper.writeValueAsString(dataclass);
        return objectMapper.readValue(serializedData, Object.class);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  public String getTaskInstance() {
    return taskInstance;
  }

  public String getTaskName() {
    return taskName;
  }

  public Object getTaskData() {
    return taskData;
  }

  public Long getId() {
    return id;
  }

  public Instant getTimeStarted() {
    return timeStarted;
  }

  public Instant getTimeFinished() {
    return timeFinished;
  }

  public boolean isSucceeded() {
    return succeeded;
  }

  public Long getDurationMs() {
    return durationMs;
  }

  public String getExceptionClass() {
    return exceptionClass;
  }

  public String getExceptionMessage() {
    return exceptionMessage;
  }

  public String getExceptionStackTrace() {
    return exceptionStackTrace;
  }
}
