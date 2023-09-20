import { FilterBy } from './getTasks';

const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string) ??
  window.location.origin + '/db-scheduler-api';

export const ALL_LOG_QUERY_KEY = `logs/all`;

export const getAllLogs = async (
  filter: FilterBy,
  searchTerm: string,
  startTime: Date | null,
  endTime: Date | null,
  taskName: string | undefined,
  taskInstance: string | undefined,
  isAsc: boolean,
  sorting: string,
) => {
  const queryParams = new URLSearchParams();

  startTime && queryParams.append('startTime', startTime.toISOString());
  endTime && queryParams.append('endTime', endTime.toISOString());
  filter && queryParams.append('filter', filter.toUpperCase());
  searchTerm && queryParams.append('searchTerm', searchTerm.trim());
  taskName && queryParams.append('taskName', taskName);
  taskInstance && queryParams.append('taskInstance', taskInstance);
  queryParams.append('asc', isAsc.toString());
  sorting && queryParams.append('sorting', sorting.toUpperCase());

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
