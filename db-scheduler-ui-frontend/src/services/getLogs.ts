import { TaskDetailsRequestParams } from 'src/models/TaskRequestParams';
import { LogResponse } from 'src/models/TasksResponse';

const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string) ??
  window.location.origin + '/db-scheduler-api';

export const ALL_LOG_QUERY_KEY = `logs/all`;

export const getLogs = async (
  params:TaskDetailsRequestParams
): Promise<LogResponse> => {
  const queryParams = new URLSearchParams();

  params.filter && queryParams.append('filter', params.filter.toUpperCase());
  params.pageNumber && queryParams.append('pageNumber', params.pageNumber.toString());
  params.limit && queryParams.append('size', params.limit.toString());
  params.sorting && queryParams.append('sorting', params.sorting.toUpperCase());
  params.asc && queryParams.append('asc', params.asc.toString());
  params.refresh && queryParams.append('refresh', params.refresh.toString());
  params.searchTerm && queryParams.append('searchTerm', params.searchTerm.trim());
  params.taskName && queryParams.append('taskName', params.taskName);
  params.taskId && queryParams.append('taskId', params.taskId);

  const response = await fetch(`${API_BASE_URL}/logs/all?${queryParams}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error(`Error fetching logs. Status: ${response.statusText}`);
  }

  return await response.json();
};
