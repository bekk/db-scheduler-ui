/*
 * Copyright (C) Bekk
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
