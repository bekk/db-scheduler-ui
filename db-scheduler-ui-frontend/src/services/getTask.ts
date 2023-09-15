import { TasksResponse } from 'src/models/TasksResponse';
import { FilterBy, PaginationParams, SortBy } from './getTasks';

const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string) ??
  window.location.origin + '/db-scheduler-api';

export const TASK_DETAILS_QUERY_KEY = `tasks/details`;

export const getTask = async (
  filter = FilterBy.All,
  { pageNumber = 1, limit = 10 }: PaginationParams,
  sorting = SortBy.Default,
  isAsc = true,
  refresh = true,
  taskName?: string,
  taskId?: string,
): Promise<TasksResponse> => {
  const queryParams = new URLSearchParams();

  queryParams.append('filter', filter.toUpperCase());
  queryParams.append('pageNumber', pageNumber.toString());
  queryParams.append('size', limit.toString());
  queryParams.append('sorting', sorting.toUpperCase());
  queryParams.append('asc', isAsc.toString());
  queryParams.append('refresh', refresh.toString());
  taskName && queryParams.append('taskName', taskName);
  taskId && queryParams.append('taskId', taskId);

  const response = await fetch(`${API_BASE_URL}/tasks/details?${queryParams}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error(`Error fetching tasks. Status: ${response.statusText}`);
  }

  return await response.json();
};
