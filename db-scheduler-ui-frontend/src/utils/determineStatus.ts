import { Task } from "src/models/Task";

export const status = ['Failed', 'Running', 'Scheduled', 'Group'] as const; 

type StatusType = typeof status[number]; 


export function determineStatus(task: Task): StatusType;
export function determineStatus(
  taskInstance: Task["taskInstance"], 
  pickedBy: Task["pickedBy"], 
  consecutiveFailures: Task["consecutiveFailures"]
): StatusType;
export function determineStatus(
  taskOrTaskInstance: Task | Task["taskInstance"], 
  pickedBy?: Task["pickedBy"], 
  consecutiveFailures?: Task["consecutiveFailures"]
): StatusType {
    if (typeof taskOrTaskInstance === "object" && 'taskName' in taskOrTaskInstance) {
      const task = taskOrTaskInstance;
      if (task.taskInstance.length > 1) return status[3];
      if (task.pickedBy[0]) return status[1];
      if (task.consecutiveFailures[0] > 0) return status[0];
      return status[2];
    } else {
      if (taskOrTaskInstance.length > 1) return status[3];
      if (pickedBy![0]) return status[1];
      if (consecutiveFailures![0] > 0) return status[0];
      return status[2];
    }
}

export function isStatus(givenStatus: StatusType, task: Task): boolean;
export function isStatus(
  givenStatus: StatusType,
  taskInstance: Task["taskInstance"],
  pickedBy: Task["pickedBy"],
  consecutiveFailures: Task["consecutiveFailures"]
): boolean;
export function isStatus(
  givenStatus: StatusType,
  taskInstanceOrTask: Task["taskInstance"] | Task,
  pickedBy?: Task["pickedBy"],
  consecutiveFailures?: Task["consecutiveFailures"]
): boolean {
  if (typeof taskInstanceOrTask === "object" && 'taskName' in taskInstanceOrTask) {
    return givenStatus === determineStatus(taskInstanceOrTask);
  } else {
    return givenStatus === determineStatus(taskInstanceOrTask, pickedBy!, consecutiveFailures!);
  }
}
