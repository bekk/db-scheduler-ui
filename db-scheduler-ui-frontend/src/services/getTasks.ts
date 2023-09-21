import { TasksResponse } from 'src/models/TasksResponse';
import { TaskRequestParams} from 'src/models/TaskRequestParams';

const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string) ??
  window.location.origin + '/db-scheduler-api';

export const TASK_QUERY_KEY = `tasks`;

export const getTasks = async (params: TaskRequestParams): Promise<TasksResponse> => {
  const queryParams = new URLSearchParams();

  params.filter && queryParams.append('filter', params.filter.toUpperCase());
  params.pageNumber && queryParams.append('pageNumber', params.pageNumber.toString());
  params.limit && queryParams.append('size', params.limit.toString());
  params.sorting && queryParams.append('sorting', params.sorting.toUpperCase());
  params.asc !==undefined  && queryParams.append('asc', params.asc.toString());
  params.refresh !==undefined  && queryParams.append('refresh', params.refresh.toString());
  params.searchTerm && queryParams.append('searchTerm', params.searchTerm.trim());


  const response = await fetch(`${API_BASE_URL}/tasks/all?${queryParams}`, {
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
