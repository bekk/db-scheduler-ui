import { FilterBy } from "./getTasks";

const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string) ??
  window.location.origin + '/db-scheduler-api';

export const ALL_LOG_QUERY_KEY = `logs/all`;

export const getAllLogs = async (filter:FilterBy,searchTerm:string) => {
  const queryParams = new URLSearchParams();
  queryParams.append('filter', filter.toUpperCase());
  queryParams.append('searchTerm', searchTerm.trim());
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
