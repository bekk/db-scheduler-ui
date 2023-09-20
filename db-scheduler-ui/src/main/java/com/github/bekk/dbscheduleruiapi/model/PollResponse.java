package com.github.bekk.dbscheduleruiapi.model;

public class PollResponse {
    private final int newFailures;
    private final int newRunning;
    private final int newTasks;
    private final int stoppedFailing;
    private final int finishedRunning;

    public PollResponse(int newFailures, int newRunning, int newTasks, int stoppedFailing, int finishedRunning) {
        this.newFailures = newFailures;
        this.newRunning = newRunning;
        this.newTasks = newTasks;
        this.stoppedFailing = stoppedFailing;
        this.finishedRunning = finishedRunning;
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

    public int getStoppedFailing() {
        return stoppedFailing;
    }

    public int getFinishedRunning() {
        return finishedRunning;
    }
}
