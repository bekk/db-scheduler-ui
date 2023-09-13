const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string) ??
  window.location.origin + '/db-scheduler-api';

const deleteTask = async (id: string, name: string) => {
  const response = await fetch(
    `${API_BASE_URL}/tasks/delete?id=${id}&name=${name}`,
    {
      method: 'POST',
    },
  );

  if (!response.ok) {
    throw new Error(`Error executing task. Status: ${response.statusText}`);
  }
};

export default deleteTask;
