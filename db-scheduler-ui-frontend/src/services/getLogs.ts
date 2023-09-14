const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string) ??
  window.location.origin + '/db-scheduler-api';

export const LOG_QUERY_KEY = `logs`;

export const getLogs = async (taskName: string, taskInstance: string, searchTerm:string) => {
  const queryParams = new URLSearchParams();

  queryParams.append('taskName', taskName);
  queryParams.append('taskInstance', taskInstance);
  queryParams.append('searchTerm', searchTerm);
  const response = await fetch(`${API_BASE_URL}/logs/id?${queryParams}`, {
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
