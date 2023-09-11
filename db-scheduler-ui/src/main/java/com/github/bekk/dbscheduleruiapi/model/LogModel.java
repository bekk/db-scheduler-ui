package com.github.bekk.dbscheduleruiapi.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.time.Instant;

public class LogModel {
    private Long id;
    private String taskName;
    private String taskInstance;
    private byte[] taskData;
    private Instant timeStarted;
    private Instant timeFinished;

    private boolean succeeded;
    private Long durationMs;
    private String exceptionClass;
    private String exceptionMessage;
    private String exceptionStackTrace;


    public LogModel(
            Long id, String taskName, String taskInstance, byte[] taskData,
            Instant timeStarted, Instant timeFinished, boolean succeeded,
            Long durationMs, String exceptionClass, String exceptionMessage, String exceptionStackTrace
    ) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskInstance() {
        return taskInstance;
    }

    public void setTaskInstance(String taskInstance) {
        this.taskInstance = taskInstance;
    }

    public byte[] getTaskData() {
        return taskData;
    }

    public void setTaskData(byte[] taskData) {
        this.taskData = taskData;
    }

    public Instant getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted(Instant timeStarted) {
        this.timeStarted = timeStarted;
    }

    public Instant getTimeFinished() {
        return timeFinished;
    }

    public void setTimeFinished(Instant timeFinished) {
        this.timeFinished = timeFinished;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getExceptionStackTrace() {
        return exceptionStackTrace;
    }

    public void setExceptionStackTrace(String exceptionStackTrace) {
        this.exceptionStackTrace = exceptionStackTrace;
    }
}
