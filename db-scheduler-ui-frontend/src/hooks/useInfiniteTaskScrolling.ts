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
import { useCallback, useState, useRef, useMemo } from 'react';
import { useInfiniteQuery, useQueryClient } from '@tanstack/react-query';
import { InfiniteScrollResponse } from 'src/models/TasksResponse';
import { FilterBy, SortBy } from 'src/models/QueryParams';
import { TaskDetailsRequestParams } from 'src/models/TaskRequestParams';
import { Log } from 'src/models/Log';
import { Task } from 'src/models/Task';

interface UseInfiniteScrollingProps<
  T extends InfiniteScrollResponse<Task | Log>,
> {
  fetchDataFunction: (params: TaskDetailsRequestParams) => Promise<T>;
  baseQueryKey: string;
  taskName?: string;
  taskInstance?: string;
}

export const useInfiniteScrolling = <
  T extends InfiniteScrollResponse<Task | Log>,
>({
  fetchDataFunction,
  taskName,
  taskInstance,
  baseQueryKey,
}: UseInfiniteScrollingProps<T>) => {
  const [currentFilter, setCurrentFilter] = useState<FilterBy>(FilterBy.All);
  const [currentSort, setCurrentSort] = useState<SortBy>(SortBy.Default);
  const [sortAsc, setSortAsc] = useState<boolean>(true);
  const [startTime, setStartTime] = useState<Date | null>(null);
  const [endTime, setEndTime] = useState<Date | null>(null);
  const [searchTermTaskName, setSearchTermTaskName] = useState<string>('');
  const [searchTermTaskInstance, setSearchTermTaskInstance] =
    useState<string>('');
  const [taskNameExactMatch, setTaskNameExactMatch] = useState<boolean>(false);
  const [taskInstanceExactMatch, setTaskInstanceExactMatch] =
    useState<boolean>(false);
  const limit = 10;

  const prevFilterRef = useRef<FilterBy>();
  const prevSortRef = useRef<SortBy>();
  const prevSortAscRef = useRef<boolean>();
  const prevSearchTermTaskNameRef = useRef<string>();
  const prevSearchTermTaskInstanceRef = useRef<string>();
  const prevSearchTermTaskNameExactMatchRef = useRef<boolean>();
  const prevSearchTermTaskInstanceExactMatchRef = useRef<boolean>();

  const queryClient = useQueryClient();

  const queryKey = useMemo(
    () => [
      baseQueryKey,
      currentFilter,
      currentSort,
      sortAsc,
      ...(startTime ? [ startTime ] : []),
      ...(endTime ? [ endTime ] : []),
      ...(taskName ? [taskName] : []),
      ...(taskInstance ? [taskInstance] : []),
      searchTermTaskName,
      searchTermTaskInstance,
      taskNameExactMatch,
      taskInstanceExactMatch,
    ],
    [
      baseQueryKey,
      currentFilter,
      currentSort,
      sortAsc,
      startTime, endTime, taskName,
      taskInstance,
      searchTermTaskName,
      searchTermTaskInstance,
      taskNameExactMatch,
      taskInstanceExactMatch,
    ],
  );

  const fetchItems = useCallback(
    ({ pageParam = 0 }) => {
      const hasFilterChanged = prevFilterRef.current !== currentFilter;
      const hasSortChanged = prevSortRef.current !== currentSort;
      const hasSortAscChanged = prevSortAscRef.current !== sortAsc;
      const hasSearchTermTaskNameChanged =
        prevSearchTermTaskNameRef.current !== searchTermTaskName;
      const hasSearchTermTaskInstanceChanged =
        prevSearchTermTaskInstanceRef.current !== searchTermTaskInstance;
      const hasSearchTermTaskNameExactMatchChanged =
        prevSearchTermTaskNameExactMatchRef.current !== taskNameExactMatch;
      const hasSearchTermTaskInstanceExactMatchChanged =
        prevSearchTermTaskInstanceExactMatchRef.current !==
        taskInstanceExactMatch;

      const shouldRefresh =
        pageParam === 0 ||
        hasFilterChanged ||
        hasSortChanged ||
        hasSortAscChanged ||
        hasSearchTermTaskNameChanged ||
        hasSearchTermTaskInstanceChanged ||
        hasSearchTermTaskNameExactMatchChanged ||
        hasSearchTermTaskInstanceExactMatchChanged;

      if (
        hasFilterChanged ||
        hasSortChanged ||
        hasSortAscChanged ||
        hasSearchTermTaskNameChanged ||
        hasSearchTermTaskInstanceChanged ||
        hasSearchTermTaskNameExactMatchChanged ||
        hasSearchTermTaskInstanceExactMatchChanged
      ) {
        queryClient.removeQueries(queryKey);
      }

      prevFilterRef.current = currentFilter;
      prevSortRef.current = currentSort;
      prevSortAscRef.current = sortAsc;
      prevSearchTermTaskNameRef.current = searchTermTaskName;
      prevSearchTermTaskInstanceRef.current = searchTermTaskInstance;
      prevSearchTermTaskNameExactMatchRef.current = taskNameExactMatch;
      prevSearchTermTaskInstanceExactMatchRef.current = taskInstanceExactMatch;

      const params = {
        filter: currentFilter,
        pageNumber: pageParam,
        limit: limit,
        sorting: currentSort,
        asc: sortAsc,
        ...(startTime ? { startTime } : {}),
        ...(endTime ? { endTime } : {}),
        refresh: shouldRefresh,
        searchTermTaskName,
        searchTermTaskInstance,
        taskNameExactMatch,
        taskInstanceExactMatch,
        size: limit,
        ...(taskName ? { taskName } : {}),
        ...(taskInstance ? { taskId: taskInstance } : {}),
      };

      return fetchDataFunction(params);
    },
    [
      currentFilter,
      currentSort,
      sortAsc,
      searchTermTaskName,
      searchTermTaskInstance,
      taskName,
      taskInstance,
      queryClient,
      queryKey,
      fetchDataFunction,
      taskNameExactMatch,
      taskInstanceExactMatch,
      startTime, endTime
    ],
  );

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    refetch,
    isFetched,
  } = useInfiniteQuery<T>(queryKey, fetchItems, {
    getNextPageParam: (lastPage, allPages) => {
      const nextPage = allPages.length + 1;
      return nextPage <= lastPage.numberOfPages ? nextPage : undefined;
    },
    refetchInterval: 0,
  });

  return {
    currentFilter,
    currentSort,
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    refetch,
    setCurrentFilter,
    setCurrentSort,
    sortAsc,
    setSortAsc,
    startTime,
    setStartTime,
    endTime,
    setEndTime,
    searchTermTaskName,
    setSearchTermTaskName,
    searchTermTaskInstance,
    setSearchTermTaskInstance,
    isDetailsView: !!taskName,
    isFetched,
    taskNameExactMatch,
    setTaskNameExactMatch,
    taskInstanceExactMatch,
    setTaskInstanceExactMatch,
  };
};
