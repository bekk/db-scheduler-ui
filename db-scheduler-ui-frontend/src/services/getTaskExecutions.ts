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
import { ExecutionPage, ExecutionStatusFilter } from 'src/models/ExecutionPage';

const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string) ??
  window.location.origin + (window.CONTEXT_PATH || '') + '/db-scheduler-api';

export const TASK_EXECUTIONS_QUERY_KEY = 'tasks/executions';

/**
 * One page of a task's executions, in execution-time order. `after` is the previous page's
 * cursor; `from` starts the list at an execution time when there is no cursor.
 */
export const getTaskExecutions = async (
  taskName: string,
  status: ExecutionStatusFilter,
  after?: string,
  from?: string,
): Promise<ExecutionPage> => {
  const queryParams = new URLSearchParams({ taskName, status });
  if (after) {
    queryParams.set('after', after);
  }
  if (from) {
    queryParams.set('from', from);
  }

  const response = await fetch(
    `${API_BASE_URL}/tasks/executions?${queryParams}`,
    { method: 'GET', headers: { 'Content-Type': 'application/json' } },
  );

  if (response.status === 401) {
    document.location.href = '/db-scheduler';
    throw new Error('Unauthorized');
  } else if (!response.ok) {
    throw new Error(
      `Error fetching executions. Status: ${response.statusText}`,
    );
  }

  return await response.json();
};
