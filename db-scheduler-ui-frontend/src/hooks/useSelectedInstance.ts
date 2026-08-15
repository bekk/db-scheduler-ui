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
import { useSearchParams } from 'react-router-dom';

// Named for the task, not the instance: Overview rows identify a task, and the endpoint
// resolves the single execution behind it.
const TASK_PARAM = 'task';

interface SelectedInstanceState {
  /** The task whose execution is open, or null when the panel is closed. */
  selected: string | null;
  select: (taskName: string) => void;
  clear: () => void;
}

/** Keeps the open instance in the query string, so reloads and shared links reopen it. */
export const useSelectedInstance = (): SelectedInstanceState => {
  const [searchParams, setSearchParams] = useSearchParams();

  const write = (taskName: string | null) => {
    const params = new URLSearchParams(searchParams);
    if (taskName) {
      params.set(TASK_PARAM, taskName);
    } else {
      params.delete(TASK_PARAM);
    }
    // Push, not replace, so the back button closes the drawer.
    setSearchParams(params);
  };

  return {
    selected: searchParams.get(TASK_PARAM),
    select: write,
    clear: () => write(null),
  };
};
