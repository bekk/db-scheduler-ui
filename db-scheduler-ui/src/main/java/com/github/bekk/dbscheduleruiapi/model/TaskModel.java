package com.github.bekk.dbscheduleruiapi.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.lang.Nullable;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TaskModel {
    @Nullable private String taskName;
    @Nullable private List<String> taskInstance;
    @Nullable private List<String> taskData; // Serialized JSON representation of data
    @Nullable private List<Instant> executionTime;
    private List<Boolean> picked;
    @Nullable private List<String> pickedBy;
    @Nullable private List<Instant> lastSuccess;
    @Nullable private Instant lastFailure;
    private int consecutiveFailures;
    @Nullable private Instant lastHeartbeat;
    private int version;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public TaskModel(
            @Nullable String taskName, @Nullable List<String> taskInstance, @Nullable List<Object> taskData,
            @Nullable List<Instant> executionTime, List<Boolean> picked, @Nullable List<String> pickedBy,
            @Nullable List<Instant> lastSuccess, @Nullable Instant lastFailure, int consecutiveFailures,
            @Nullable Instant lastHeartbeat, int version
    ) {
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

    @Nullable
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(@Nullable String taskName) {
        this.taskName = taskName;
    }

    @Nullable
    public List<String> getTaskInstance() {
        return taskInstance;
    }

    public void setTaskInstance(@Nullable List<String> taskInstance) {
        this.taskInstance = taskInstance;
    }

    @Nullable
    public List<String> getActualTaskData() {
            return taskData;
    }

    public List<String> getTaskData() {
        return this.taskData;
    }

    public void serializeTaskData(@Nullable List<Object> taskData) {
            System.out.println("TaskData: " + taskData);
            try {
                assert taskData != null;
                this.taskData = Arrays.asList(objectMapper.writeValueAsString(taskData.get(0)));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            System.out.println(this.taskData);
    }
    public void setTaskData(List<String> taskData) {
        this.taskData = taskData;
    }

    @Nullable
    public List<Instant> getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(@Nullable List<Instant> executionTime) {
        this.executionTime = executionTime;
    }

    public List<Boolean> isPicked() {
        return picked;
    }

    public void setPicked(List<Boolean> picked) {
        this.picked = picked;
    }

    @Nullable
    public List<String> getPickedBy() {
        return pickedBy;
    }

    public void setPickedBy(@Nullable List<String> pickedBy) {
        this.pickedBy = pickedBy;
    }

    @Nullable
    public List<Instant> getLastSuccess() {
        return lastSuccess;
    }

    public void setLastSuccess(@Nullable List<Instant> lastSuccess) {
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