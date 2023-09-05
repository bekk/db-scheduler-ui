package com.github.bekk.dbscheduleruiapi.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;

public class TaskModel {
    private String taskName;
    private String taskInstance;
    private String taskData; // Serialized JSON representation of data
    private Instant executionTime;
    private boolean picked;
    private String pickedBy;
    private Instant lastSuccess;
    private Instant lastFailure;
    private int consecutiveFailures;
    private Instant lastHeartbeat;
    private int version;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public TaskModel(
            String taskName, String taskInstance, Object taskData,
            Instant executionTime, boolean picked, String pickedBy,
            Instant lastSuccess, Instant lastFailure, int consecutiveFailures,
            Instant lastHeartbeat, int version
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

    public void setTaskData(Object taskData) {
        try {
            this.taskData = objectMapper.writeValueAsString(taskData);
        } catch (JsonProcessingException e) {
            // Handle the error appropriately
            this.taskData = null;
        }
    }

    
    public Instant getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Instant executionTime) {
        this.executionTime = executionTime;
    }

    public boolean isPicked() {
        return picked;
    }

    public void setPicked(boolean picked) {
        this.picked = picked;
    }

    
    public String getPickedBy() {
        return pickedBy;
    }

    public void setPickedBy(String pickedBy) {
        this.pickedBy = pickedBy;
    }

    
    public Instant getLastSuccess() {
        return lastSuccess;
    }

    public void setLastSuccess(Instant lastSuccess) {
        this.lastSuccess = lastSuccess;
    }

    
    public Instant getLastFailure() {
        return lastFailure;
    }

    public void setLastFailure(Instant lastFailure) {
        this.lastFailure = lastFailure;
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public void setConsecutiveFailures(int consecutiveFailures) {
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
