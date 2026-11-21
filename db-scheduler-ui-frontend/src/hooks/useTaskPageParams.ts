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
import {
  EXECUTION_STATUS_FILTERS,
  ExecutionStatusFilter,
} from 'src/models/ExecutionPage';

const STATUS_PARAM = 'status';
const FROM_PARAM = 'from';
const INSTANCE_PARAM = 'instance';

interface TaskPageParams {
  /** The chosen tab, or null when the page should pick its default. */
  status: ExecutionStatusFilter | null;
  setStatus: (status: ExecutionStatusFilter) => void;
  /** ISO instant the list starts at, or null for the earliest execution. */
  from: string | null;
  setFrom: (from: string | null) => void;
  /** The instance open in the drawer. */
  instance: string | null;
  openInstance: (id: string) => void;
  closeInstance: () => void;
}

/** The task page's view state, kept in the query string so reloads and shared links restore it. */
export const useTaskPageParams = (): TaskPageParams => {
  const [searchParams, setSearchParams] = useSearchParams();

  const write = (
    name: string,
    value: string | null,
    options: { replace: boolean },
  ) => {
    const params = new URLSearchParams(searchParams);
    if (value) {
      params.set(name, value);
    } else {
      params.delete(name);
    }
    setSearchParams(params, options);
  };

  // Unknown values are dropped: the query string is user-editable.
  const rawStatus = searchParams.get(STATUS_PARAM);
  const status =
    EXECUTION_STATUS_FILTERS.find((value) => value === rawStatus) ?? null;

  return {
    status,
    // Tabs and the start time change the view; replace so they do not fill the back button.
    setStatus: (next) => write(STATUS_PARAM, next, { replace: true }),
    from: searchParams.get(FROM_PARAM),
    setFrom: (next) => write(FROM_PARAM, next, { replace: true }),
    instance: searchParams.get(INSTANCE_PARAM),
    // Push, so the back button closes the drawer.
    openInstance: (id) => write(INSTANCE_PARAM, id, { replace: false }),
    closeInstance: () => write(INSTANCE_PARAM, null, { replace: false }),
  };
};
