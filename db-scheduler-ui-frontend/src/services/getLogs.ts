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

import { TaskDetailsRequestParams } from 'src/models/TaskRequestParams';
import { LogResponse } from 'src/models/TasksResponse';

const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string) ??
  window.location.origin + (window.CONTEXT_PATH || '') + '/db-scheduler-api';

export const ALL_LOG_QUERY_KEY = `logs/all`;

export const getLogs = async (
  params: TaskDetailsRequestParams,
): Promise<LogResponse> => {
  const queryParams = new URLSearchParams();

  params.filter && queryParams.append('filter', params.filter.toUpperCase());
  params.pageNumber &&
    queryParams.append('pageNumber', params.pageNumber.toString());
  params.limit && queryParams.append('size', params.limit.toString());
  params.sorting && queryParams.append('sorting', params.sorting.toUpperCase());
  params.asc!==undefined && queryParams.append('asc', params.asc.toString());
  params.startTime && queryParams.append('startTime', params.startTime.toISOString());
  params.endTime && queryParams.append('endTime', params.endTime.toISOString());
  params.refresh!==undefined && queryParams.append('refresh', params.refresh.toString());
  params.searchTermTaskName &&
    queryParams.append('searchTermTaskName', params.searchTermTaskName.trim());
  params.searchTermTaskInstance &&
    queryParams.append(
      'searchTermTaskInstance',
      params.searchTermTaskInstance.trim(),
    );
  params.taskName && queryParams.append('taskName', params.taskName);
  params.taskId && queryParams.append('taskId', params.taskId);
  params.taskNameExactMatch !== undefined &&
    queryParams.append(
      'taskNameExactMatch',
      params.taskNameExactMatch.toString(),
    );
  params.taskInstanceExactMatch !== undefined &&
    queryParams.append(
      'taskInstanceExactMatch',
      params.taskInstanceExactMatch.toString(),
    );

  const response = await fetch(`${API_BASE_URL}/logs/all?${queryParams}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (response.status == 401) {
    document.location.href = '/db-scheduler';
  } else if (!response.ok) {
    throw new Error(`Error fetching logs. Status: ${response.statusText}`);
  }

  return await response.json();
};
