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
import { TaskDetailsRequestParams } from 'src/models/TaskRequestParams';
import { PollResponse } from 'src/models/PollResponse';

type PollFunctionType = (
  params: TaskDetailsRequestParams,
) => Promise<PollResponse>;

export const usePolling = (
  pollFunction: PollFunctionType,
  pollKey: string,
  params: TaskDetailsRequestParams,
) => {
  const { data, refetch: repoll } = useQuery([pollKey], () =>
    pollFunction({
      filter: params.filter,
      sorting: params.sorting,
      asc: params.asc,
      startTime: params.startTime,
      endTime: params.endTime,
      taskName: params.taskName,
      taskId: params.taskId,
      searchTermTaskName: params.searchTermTaskName,
      searchTermTaskInstance: params.searchTermTaskInstance,
      taskInstanceExactMatch: params.taskInstanceExactMatch,
      taskNameExactMatch: params.taskNameExactMatch,
    }),
  );

  return { data, repoll };
};
