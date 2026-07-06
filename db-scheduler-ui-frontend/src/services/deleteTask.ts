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
import { getApiBaseUrl, redirectToUi } from 'src/utils/runtimeConfig';

const API_BASE_URL = getApiBaseUrl();

const deleteTask = async (id: string, name: string) => {
  const queryParams = new URLSearchParams({ id, name });

  const response = await fetch(
    `${API_BASE_URL}/tasks/delete?${queryParams}`,
    {
      method: 'POST',
    },
  );

  if (response.status == 401) {
    redirectToUi();
  } else if (!response.ok) {
    throw new Error(`Error executing task. Status: ${response.statusText}`);
  }
};

export default deleteTask;
