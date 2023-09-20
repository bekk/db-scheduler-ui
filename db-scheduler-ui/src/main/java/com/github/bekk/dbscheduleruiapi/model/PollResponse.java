package com.github.bekk.dbscheduleruiapi.model;

public class PollResponse {
    private final int newFailures;
    private final int newRunning;
    private final int newTasks;
    public PollResponse(int newFailures, int newRunning, int newTasks) {
        this.newFailures = newFailures;
        this.newRunning = newRunning;
        this.newTasks = newTasks;
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

}
