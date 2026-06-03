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

/** Worst (most severe) state present among the instances of a task. */
export type WorstStatus = 'FAILING' | 'RUNNING' | 'SCHEDULED' | 'DORMANT';

export interface OverviewCounts {
  failing: number;
  running: number;
  scheduled: number;
}

/**
 * One aggregated Overview row — all instances of a single task name collapsed into a health
 * summary. Mirrors the backend `OverviewTask` record. Instant fields arrive as ISO-8601 strings.
 */
export interface OverviewTask {
  taskName: string;
  /** `true`/`false` when task definitions are known; `null` in degraded mode. */
  recurring: boolean | null;
  instanceCount: number;
  counts: OverviewCounts;
  worstStatus: WorstStatus;
  nextExecutionTime: string | null;
  lastSuccess: string | null;
  lastFailure: string | null;
  maxConsecutiveFailures: number;
}
