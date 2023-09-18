import { useCallback, useState, useEffect, useRef } from 'react';
import { useInfiniteQuery } from '@tanstack/react-query';
import { FilterBy, SortBy, getTasks } from 'src/services/getTasks';
import { getTask } from 'src/services/getTask';

interface UseInfiniteTaskScrollingProps {
    getTasksFunction: typeof getTasks | typeof getTask;
    taskName?: string;
    baseQueryKey: string;
}

export const useInfiniteTaskScrolling = ({ getTasksFunction, taskName, baseQueryKey }: UseInfiniteTaskScrollingProps) => {
  const [currentFilter, setCurrentFilter] = useState<FilterBy>(FilterBy.All);
  const [currentSort, setCurrentSort] = useState<SortBy>(SortBy.Default);
  const [sortAsc, setSortAsc] = useState<boolean>(true);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const limit = 10;
  const [refetchInterval, setRefetchInterval] = useState<number | false>(2000);

  const prevFilterRef = useRef<FilterBy>();
  const prevSortRef = useRef<SortBy>();
  const prevSortAscRef = useRef<boolean>();
  const prevSearchTermRef = useRef<string>();

  const fetchTasks = useCallback(
    ({ pageParam = 0 }) => {
      const hasFilterChanged = prevFilterRef.current !== currentFilter;
      const hasSortChanged = prevSortRef.current !== currentSort;
      const hasSortAscChanged = prevSortAscRef.current !== sortAsc;
      const hasSearchTermChanged = prevSearchTermRef.current !== searchTerm;

      prevFilterRef.current = currentFilter;
      prevSortRef.current = currentSort;
      prevSortAscRef.current = sortAsc;
      prevSearchTermRef.current = searchTerm;

      const shouldRefresh = pageParam === 0 || hasFilterChanged || hasSortChanged || hasSortAscChanged || hasSearchTermChanged;

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
          searchTerm
        );
      }
    },
    [currentFilter, currentSort, sortAsc, taskName, getTasksFunction, searchTerm]
  );
  
  const queryKey = [baseQueryKey, currentFilter, currentSort, sortAsc, ...(taskName ? [taskName] : []), searchTerm];

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, refetch } = useInfiniteQuery(
    queryKey,
    fetchTasks,
    {
      getNextPageParam: (lastPage, allPages) => {
        const nextPage = allPages.length + 1;
        return nextPage <= lastPage.numberOfPages ? nextPage : undefined;
      },
      refetchInterval: refetchInterval,
    }
  );

  useEffect(() => {
    if ((data?.pages?.length || 0) > 1) {
      setRefetchInterval(false);
    } else {
      setRefetchInterval(2000);
    }
  }, [data]);

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
    isDetailsView: !!taskName
  };
};
