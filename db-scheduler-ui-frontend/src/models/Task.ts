export type Task = {
  taskName: string;
  taskInstance: string[];
  taskData: (string|null)[];
  actualTaskData: (object|null)[];
  executionTime: Date[];
  picked: boolean;
  pickedBy: (string|null)[];
  lastSuccess: Date[] | null;
  lastFailure: Date | null;
  consecutiveFailures: number[];
  lastHeartbeat: Date | null;
  version: number;
};