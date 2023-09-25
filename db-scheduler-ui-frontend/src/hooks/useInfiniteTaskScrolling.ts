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

interface UseInfiniteScrollingProps<T extends InfiniteScrollResponse<Task | Log>> {
  fetchDataFunction: (params: TaskDetailsRequestParams) => Promise<T>;
  baseQueryKey: string;
  taskName?: string;
  taskInstance?: string;
}

export const useInfiniteScrolling = <T extends InfiniteScrollResponse<Task | Log>>({
  fetchDataFunction,
  taskName,
  taskInstance,
  baseQueryKey,
}: UseInfiniteScrollingProps<T>) => {
  const [currentFilter, setCurrentFilter] = useState<FilterBy>(FilterBy.All);
  const [currentSort, setCurrentSort] = useState<SortBy>(SortBy.Default);
  const [sortAsc, setSortAsc] = useState<boolean>(true);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const limit = 10;

  const prevFilterRef = useRef<FilterBy>();
  const prevSortRef = useRef<SortBy>();
  const prevSortAscRef = useRef<boolean>();
  const prevSearchTermRef = useRef<string>();

  const queryClient = useQueryClient();

  const queryKey = useMemo(() => [
    baseQueryKey,
    currentFilter,
    currentSort,
    sortAsc,
    ...(taskName ? [taskName] : []),
    ...(taskInstance ? [taskInstance] : []),
    searchTerm,
  ], [baseQueryKey, currentFilter, currentSort, sortAsc, taskName,taskInstance, searchTerm]);
  

  const fetchItems = useCallback(
    ({ pageParam = 0 }) => {
      const hasFilterChanged = prevFilterRef.current !== currentFilter;
      const hasSortChanged = prevSortRef.current !== currentSort;
      const hasSortAscChanged = prevSortAscRef.current !== sortAsc;
      const hasSearchTermChanged = prevSearchTermRef.current !== searchTerm;

      const shouldRefresh = pageParam === 0 || hasFilterChanged || hasSortChanged || hasSortAscChanged || hasSearchTermChanged;

      if (hasFilterChanged || hasSortChanged || hasSortAscChanged || hasSearchTermChanged) {
        queryClient.removeQueries(queryKey);
      }

      prevFilterRef.current = currentFilter;
      prevSortRef.current = currentSort;
      prevSortAscRef.current = sortAsc;
      prevSearchTermRef.current = searchTerm;

      const params = {
        filter: currentFilter,
        pageNumber: pageParam,
        limit: limit,
        sorting: currentSort,
        asc: sortAsc,
        refresh: shouldRefresh,
        searchTerm,
        size: limit,
        ...(taskName ? { taskName } : {}),
        ...(taskInstance ? { taskId:taskInstance } : {}),
      };

      return fetchDataFunction(params);
    },
    [currentFilter, currentSort, sortAsc, searchTerm, taskName, taskInstance, queryClient, queryKey, fetchDataFunction],
  );



  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, refetch, isFetched } =
    useInfiniteQuery<T>(queryKey, fetchItems, {
      getNextPageParam: (lastPage, allPages) => {
        const nextPage = allPages.length + 1;
        return nextPage <= lastPage.numberOfPages ? nextPage : undefined;
      },refetchInterval:0,
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
    searchTerm,
    setSearchTerm,
    isDetailsView: !!taskName,
    isFetched
  };
};
