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

export type InstanceStatus = 'FAILED' | 'RUNNING' | 'SCHEDULED';

export interface InstanceRun {
  id: number;
  succeeded: boolean;
  timeStarted: string;
  durationMs: number;
  exceptionClass: string | null;
  exceptionMessage: string | null;
  /** Only populated on `lastFailure` — the run list never carries traces. */
  stackTrace: string | null;
}

export interface InstanceHistory {
  lastFailure: InstanceRun | null;
  recentRuns: InstanceRun[];
}

/**
 * One execution, as `GET /tasks/instance` returns it.
 *
 * Version and last heartbeat are absent because db-scheduler exposes neither.
 */
export interface InstanceDetail {
  taskName: string;
  id: string;
  status: InstanceStatus;
  executionTime: string | null;
  picked: boolean;
  pickedBy: string | null;
  lastSuccess: string | null;
  lastFailure: string | null;
  consecutiveFailures: number;
  taskData: object | null;
  /** `null` when `db-scheduler-ui.history` is off — no log table, so nothing to show. */
  history: InstanceHistory | null;
}
