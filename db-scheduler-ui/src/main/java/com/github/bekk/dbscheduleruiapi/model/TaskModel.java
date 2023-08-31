package com.github.bekk.dbscheduleruiapi.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.lang.Nullable;

import java.time.Instant;

public class TaskModel {
    @Nullable private String taskName;
    @Nullable private String taskInstance;
    @Nullable private String taskData; // Serialized JSON representation of data
    @Nullable private Instant executionTime;
    private boolean picked;
    @Nullable private String pickedBy;
    @Nullable private Instant lastSuccess;
    @Nullable private Instant lastFailure;
    private int consecutiveFailures;
    @Nullable private Instant lastHeartbeat;
    private int version;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public TaskModel(
            @Nullable String taskName, @Nullable String taskInstance, @Nullable Object taskData,
            @Nullable Instant executionTime, boolean picked, @Nullable String pickedBy,
            @Nullable Instant lastSuccess, @Nullable Instant lastFailure, int consecutiveFailures,
            @Nullable Instant lastHeartbeat, int version
    ) {
        this.taskName = taskName;
        this.taskInstance = taskInstance;
        setTaskData(taskData);
        this.executionTime = executionTime;
        this.picked = picked;
        this.pickedBy = pickedBy;
        this.lastSuccess = lastSuccess;
        this.lastFailure = lastFailure;
        this.consecutiveFailures = consecutiveFailures;
        this.lastHeartbeat = lastHeartbeat;
        this.version = version;
    }

    @Nullable
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(@Nullable String taskName) {
        this.taskName = taskName;
    }

    @Nullable
    public String getTaskInstance() {
        return taskInstance;
    }

    public void setTaskInstance(@Nullable String taskInstance) {
        this.taskInstance = taskInstance;
    }

    @Nullable
    public Object getActualTaskData() {
        try {
            return objectMapper.readValue(taskData, Object.class);
        } catch (JsonProcessingException e) {
            // Handle the error appropriately
            return null;
        }
    }

    public String getTaskData() {
        return this.taskData;
    }

    public void setTaskData(@Nullable Object taskData) {
        try {
            this.taskData = objectMapper.writeValueAsString(taskData);
        } catch (JsonProcessingException e) {
            // Handle the error appropriately
            this.taskData = null;
        }
    }

    @Nullable
    public Instant getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(@Nullable Instant executionTime) {
        this.executionTime = executionTime;
    }

    public boolean isPicked() {
        return picked;
    }

    public void setPicked(boolean picked) {
        this.picked = picked;
    }

    @Nullable
    public String getPickedBy() {
        return pickedBy;
    }

    public void setPickedBy(@Nullable String pickedBy) {
        this.pickedBy = pickedBy;
    }

    @Nullable
    public Instant getLastSuccess() {
        return lastSuccess;
    }

    public void setLastSuccess(@Nullable Instant lastSuccess) {
        this.lastSuccess = lastSuccess;
    }

    @Nullable
    public Instant getLastFailure() {
        return lastFailure;
    }

    public void setLastFailure(@Nullable Instant lastFailure) {
        this.lastFailure = lastFailure;
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public void setConsecutiveFailures(int consecutiveFailures) {
        this.consecutiveFailures = consecutiveFailures;
    }

    @Nullable
    public Instant getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(@Nullable Instant lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}