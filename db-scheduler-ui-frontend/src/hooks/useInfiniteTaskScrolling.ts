import { useCallback, useState, useRef, useMemo } from 'react';
import { useInfiniteQuery, useQueryClient } from '@tanstack/react-query';
import { FilterBy, SortBy, getTasks } from 'src/services/getTasks';
import { getTask } from 'src/services/getTask';
import { TasksResponse } from 'src/models/TasksResponse';

interface UseInfiniteTaskScrollingProps {
  getTasksFunction: typeof getTasks | typeof getTask;
  taskName?: string;
  baseQueryKey: string;
}

export const useInfiniteTaskScrolling = ({
  getTasksFunction,
  taskName,
  baseQueryKey,
}: UseInfiniteTaskScrollingProps) => {
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
    searchTerm,
  ], [baseQueryKey, currentFilter, currentSort, sortAsc, taskName, searchTerm]);
  

  const fetchTasks = useCallback(
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

      if (taskName) {
        return getTasksFunction(
          currentFilter,
          { pageNumber: pageParam, limit: limit },
          currentSort,
          sortAsc,
          shouldRefresh,
          searchTerm,
          taskName,
        );
      } else {
        return getTasksFunction(
          currentFilter,
          { pageNumber: pageParam, limit: limit },
          currentSort,
          sortAsc,
          shouldRefresh,
          searchTerm,
        );
      }
    },
    [currentFilter, currentSort, sortAsc, searchTerm, taskName, queryClient, queryKey, getTasksFunction],
  );



  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, refetch } =
    useInfiniteQuery<TasksResponse>(queryKey, fetchTasks, {
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
  };
};
