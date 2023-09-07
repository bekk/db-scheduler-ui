const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string) ??
  window.location.origin + '/api';

export const getLogs = async (taskName: string) => {
  const queryParams = new URLSearchParams();

  queryParams.append('taskName', taskName);
  const response = await fetch(`${API_BASE_URL}/logs?${queryParams}`, {
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
