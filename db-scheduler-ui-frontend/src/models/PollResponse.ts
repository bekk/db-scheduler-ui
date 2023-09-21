export interface PollResponse {
    newFailures: number;
    newRunning: number;
    newTasks: number;
    stoppedFailing: number;
    finishedRunning: number;
  }