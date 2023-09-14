package com.github.bekk.dbscheduleruiapi.model;

import java.time.Instant;

public class LogModel {
  private final Long id;
  private final String taskName;
  private final String taskInstance;
  private final String taskData;
  private final Instant timeStarted;
  private final Instant timeFinished;
  private final boolean succeeded;
  private final Long durationMs;
  private final String exceptionClass;
  private final String exceptionMessage;
  private final String exceptionStackTrace;

  public LogModel(
      Long id,
      String taskName,
      String taskInstance,
      String taskData,
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
    this.taskData = taskData;
    this.timeStarted = timeStarted;
    this.timeFinished = timeFinished;
    this.succeeded = succeeded;
    this.durationMs = durationMs;
    this.exceptionClass = exceptionClass;
    this.exceptionMessage = exceptionMessage;
    this.exceptionStackTrace = exceptionStackTrace;
  }

  public String getTaskInstance() {
    return taskInstance;
  }

  public String getTaskName() {
    return taskName;
  }

  public String getTaskData() {return taskData;}

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
