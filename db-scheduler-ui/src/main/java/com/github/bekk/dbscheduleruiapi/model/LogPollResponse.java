package com.github.bekk.dbscheduleruiapi.model;

public class LogPollResponse {
    public LogPollResponse(int newFailures, int newSucceeded) {
        this.newFailures = newFailures;
        this.newSucceeded = newSucceeded;
    }

    private final int newFailures;

    public int getNewFailures() {
        return newFailures;
    }

    public int getNewSucceeded() {
        return newSucceeded;
    }

    private final int newSucceeded;


}
