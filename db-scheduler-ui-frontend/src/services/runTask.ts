/*
 * Copyright (C) Bekk
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string) ??
  window.location.origin + (window.CONTEXT_PATH || '') + '/db-scheduler-api';

const runTask = async (id: string, name: string, scheduleTime?:Date) => {

  const queryParams = new URLSearchParams();

  queryParams.append('id', id);
  queryParams.append('name', name);
  if (scheduleTime) {
      queryParams.append('scheduleTime', scheduleTime.toISOString());
  } else {
      queryParams.append('scheduleTime', new Date().toISOString());
  }

  const response = await fetch(
    `${API_BASE_URL}/tasks/rerun?${queryParams}`,
    {
      method: 'POST',
    },
  );

  if (response.status == 401) {
    document.location.href = '/db-scheduler';
  } else if (!response.ok) {
    throw new Error(`Error executing task. Status: ${response.statusText}`);
  }
};

export default runTask;
