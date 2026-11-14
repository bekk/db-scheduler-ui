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
import { useQuery } from '@tanstack/react-query';
import { InstanceDetail } from 'src/models/InstanceDetail';
import {
  getInstanceDetail,
  INSTANCE_QUERY_KEY,
  InstanceLookupError,
} from 'src/services/getInstanceDetail';

// Repeats the app-wide interval from App.tsx, because refetchInterval has to vary per state.
const POLL_MS = 2000;

/**
 * What the panel should be showing.
 *
 * - `gone` — the execution is not there: it finished, or someone deleted it
 * - `ambiguous` — the task no longer has exactly one execution
 */
export type InstanceDetailStatus =
  | 'loading'
  | 'ready'
  | 'gone'
  | 'ambiguous'
  | 'error';

export interface InstanceDetailResult {
  instance: InstanceDetail | null;
  status: InstanceDetailStatus;
  refetch: () => void;
}

/**
 * Loads one execution from `GET /tasks/instance`. The run log arrives in the same response, so a
 * `null` history means `db-scheduler-ui.history` is off.
 */
export const useInstanceDetail = (
  taskName: string | null,
): InstanceDetailResult => {
  const query = useQuery(
    [INSTANCE_QUERY_KEY, taskName],
    () => getInstanceDetail(taskName as string),
    {
      enabled: !!taskName,
      // A missing or ambiguous instance will not change on retry.
      retry: (failureCount, error) =>
        !(error instanceof InstanceLookupError) && failureCount < 3,
      // Nor on a poll; without this the app-wide interval repeats it every 2s.
      refetchInterval: (_data, q) =>
        q.state.error instanceof InstanceLookupError ? false : POLL_MS,
    },
  );

  return {
    instance: query.data ?? null,
    status: statusOf(query.data, query.error, query.isLoading),
    refetch: () => void query.refetch(),
  };
};

const statusOf = (
  data: InstanceDetail | undefined,
  error: unknown,
  isLoading: boolean,
): InstanceDetailStatus => {
  if (error instanceof InstanceLookupError) {
    return error.problem === 'GONE' ? 'gone' : 'ambiguous';
  }
  if (error) {
    return 'error';
  }
  // Data outlives a background refetch, so the panel keeps showing it rather than reloading.
  return data ? 'ready' : isLoading ? 'loading' : 'error';
};
