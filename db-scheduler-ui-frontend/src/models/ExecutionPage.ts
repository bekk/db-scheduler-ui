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
import { InstanceStatus } from 'src/models/InstanceDetail';

/** The tabs of the task page; the server reads them case-insensitively. */
export type ExecutionStatusFilter =
  | 'all'
  | 'failing'
  | 'running'
  | 'overdue'
  | 'scheduled';

export const EXECUTION_STATUS_FILTERS: ExecutionStatusFilter[] = [
  'all',
  'failing',
  'running',
  'overdue',
  'scheduled',
];

/** One execution as the task page lists it, from `GET /tasks/executions`. */
export interface ExecutionRow {
  taskInstance: string;
  status: InstanceStatus;
  executionTime: string;
  picked: boolean;
  pickedBy: string | null;
  lastSuccess: string | null;
  lastFailure: string | null;
  consecutiveFailures: number;
  /** Shortened JSON of the task data; null when task data is hidden or absent. */
  taskDataPreview: string | null;
}

export interface ExecutionPage {
  items: ExecutionRow[];
  /** Cursor for the following page; null when this is the last one. */
  next: string | null;
}
