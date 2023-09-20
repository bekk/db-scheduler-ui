export type Log = {
  id: number;
  taskName: string;
  taskInstance: string;
  taskData: object | null;
  pickedBy: string | null;
  timeStarted: Date;
  timeFinished: Date;
  succeeded: boolean;
  durationMs: number;
  exceptionClass: string | null;
  exceptionMessage: string | null;
  exceptionStackTrace: string | null;
};
