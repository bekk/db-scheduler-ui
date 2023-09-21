import { PollResponse } from 'src/models/PollResponse';
import { TaskDetailsRequestParams } from 'src/models/TaskRequestParams';
  
const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string) ??
  window.location.origin + '/db-scheduler-api';

export const POLL_TASKS_QUERY_KEY = `tasks/poll`;

export const pollTasks = async (params: TaskDetailsRequestParams): Promise<PollResponse> => {
  const queryParams = new URLSearchParams();

  queryParams.append('filter', params.filter.toUpperCase());
  params.pageNumber && queryParams.append('pageNumber', params.pageNumber.toString());
  params.size && queryParams.append('size', params.size.toString());
  params.sorting && queryParams.append('sorting', params.sorting.toUpperCase());
  params.asc && queryParams.append('asc', params.asc.toString());
  params.searchTerm && queryParams.append('searchTerm', params.searchTerm.trim());
  params.startTime && queryParams.append('startTime', params.startTime.toISOString());
  params.endTime && queryParams.append('endTime', params.endTime.toISOString());
  params.refresh && queryParams.append('refresh', params.refresh.toString());

  const response = await fetch(`${API_BASE_URL}/tasks/poll?${queryParams}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error(`Error polling tasks. Status: ${response.statusText}`);
  }

  return await response.json();
};
