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

// Matches the app-wide interval set in App.tsx; restated here because the polling has to be
// switchable per query state, which the global default cannot express.
const POLL_MS = 2000;

/**
 * What the panel should be showing. One value rather than a handful of booleans, so the states
 * cannot contradict each other and the renderer is a switch.
 *
 * - `gone` — the execution is not there: it finished, or someone deleted it
 * - `ambiguous` — the task no longer has exactly one execution, so "the" execution is a guess
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
 * Loads one execution from `GET /tasks/instance`.
 *
 * Everything the panel shows arrives in that one response, run log included, so there is no
 * second request to sequence and no config flag to consult: a `null` history *is* the answer
 * to "is history enabled".
 */
export const useInstanceDetail = (
  taskName: string | null,
): InstanceDetailResult => {
  const query = useQuery(
    [INSTANCE_QUERY_KEY, taskName],
    () => getInstanceDetail(taskName as string),
    {
      enabled: !!taskName,
      // A missing or ambiguous instance is an answer, not a hiccup — retrying cannot change it.
      retry: (failureCount, error) =>
        !(error instanceof InstanceLookupError) && failureCount < 3,
      // ...and neither can polling. Without this the app-wide 2s interval re-asks a question
      // that has already been answered, for as long as the drawer stays open.
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
  // Data outlives a background refetch, so an open panel keeps showing the instance instead of
  // blanking every couple of seconds.
  return data ? 'ready' : isLoading ? 'loading' : 'error';
};
