import { TasksResponse } from 'src/models/TasksResponse';
import { TaskDetailsRequestParams} from 'src/models/TaskRequestParams';

const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string) ??
  window.location.origin + '/db-scheduler-api';

export const TASK_DETAILS_QUERY_KEY = `tasks/details`;

export const getTask = async (
  params:TaskDetailsRequestParams
): Promise<TasksResponse> => {
  const queryParams = new URLSearchParams();

  params.filter && queryParams.append('filter', params.filter.toUpperCase());
  params.pageNumber && queryParams.append('pageNumber', params.pageNumber.toString());
  params.limit && queryParams.append('size', params.limit.toString());
  params.sorting && queryParams.append('sorting', params.sorting.toUpperCase());
  params.asc !==undefined && queryParams.append('asc', params.asc.toString());
  params.refresh !==undefined  && queryParams.append('refresh', params.refresh.toString());
  params.searchTerm && queryParams.append('searchTerm', params.searchTerm.trim());
  params.taskName && queryParams.append('taskName', params.taskName);
  params.taskId && queryParams.append('taskId', params.taskId);



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
