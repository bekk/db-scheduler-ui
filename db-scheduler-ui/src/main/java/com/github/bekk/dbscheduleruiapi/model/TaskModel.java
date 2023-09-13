package com.github.bekk.dbscheduleruiapi.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
            String taskName, List<String> taskInstance, Object inputTaskData,
            List<Instant> executionTime, List<Boolean> picked, List<String> pickedBy,
            List<Instant> lastSuccess, Instant lastFailure, List<Integer> consecutiveFailures,
            Instant lastHeartbeat, int version
    ) {
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

    private List<Object> serializeTaskData(Object inputTaskData) {
        try {
            String serializedData = objectMapper.writeValueAsString(inputTaskData);
            return Collections.singletonList(objectMapper.readValue(serializedData, Object.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

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

    public List<Object> getTaskData() {
        return taskData;
    }

    public void setTaskData(Object inputTaskData) {
        this.taskData = serializeTaskData(inputTaskData);
    }


    public List<Instant> getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime( List<Instant> executionTime) {
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

    public void setPickedBy( List<String> pickedBy) {
        this.pickedBy = pickedBy;
    }


    public List<Instant> getLastSuccess() {
        return lastSuccess;
    }

    public void setLastSuccess( List<Instant> lastSuccess) {
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

    public int getVersion() {
        return version;
    }

}
