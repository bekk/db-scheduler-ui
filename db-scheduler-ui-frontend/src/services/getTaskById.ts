const API_BASE_URL: string = import.meta.env.VITE_API_BASE_URL as string ?? window.location.origin + '/api';

const getTaskById = async (id: number) => {
  const response = await fetch(`${API_BASE_URL}:8080/tasks/${id}`);
  if (!response.ok) {
    throw new Error(
      `Error fetching task with ID: ${id}. Status: ${response.statusText}`,
    );
  }
  return await response.json();
};

export default getTaskById;
