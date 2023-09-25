export interface PollResponse {
    newFailures: number;
    newRunning: number;
    newTasks: number;
    newSucceeded?: number;
    stoppedFailing: number;
    finishedRunning: number;
  }