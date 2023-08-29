import { TasksResponse } from "src/models/TasksResponse";

const API_BASE_URL: string = import.meta.env.VITE_API_BASE_URL as string;

export enum FilterBy {
  All = 'All',
  Failed = 'Failed',
  Running = 'Running',
  Scheduled = 'Scheduled',
}

export interface PaginationParams {
  pageNumber: number;
  limit: number;
}
export enum SortBy {
  Default = 'Default',
  Name = 'Name',
}

export const TASK_QUERY_KEY = `tasks`;

export const getTasks = async (
  filter = FilterBy.All, 
  { pageNumber = 1, limit = 10 }: PaginationParams,
  sorting = SortBy.Default,
): Promise<TasksResponse> => {
  const queryParams = new URLSearchParams();
  if (filter !== FilterBy.All) {
    queryParams.append('filter', filter.toUpperCase());
  }
  queryParams.append('page', pageNumber.toString());
  queryParams.append('size', limit.toString());

  if (sorting !== SortBy.Default) {
    queryParams.append('sorting', sorting.toUpperCase());
  }

  const response = await fetch(`${API_BASE_URL}/tasks?${queryParams}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    }
  });

  if (!response.ok) {
    throw new Error(`Error fetching tasks. Status: ${response.statusText}`);
  }

  return await response.json();
};
