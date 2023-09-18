import { useCallback, useState, useEffect } from 'react';
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

  const fetchTasks = useCallback( // TODO: Support logs
    ({ pageParam = 0 }) => {
      if (taskName) {
        return getTasksFunction(
          currentFilter,
          { pageNumber: pageParam, limit: limit },
          currentSort,
          sortAsc,
          pageParam === 0 ? true : false,
          searchTerm,
          taskName,
        );
      } else {
        return getTasksFunction(
          currentFilter,
          { pageNumber: pageParam, limit: limit },
          currentSort,
          sortAsc,
          pageParam === 0 ? true : false,
          searchTerm
        );
      }
    },
    [currentFilter, currentSort, sortAsc, taskName, getTasksFunction,searchTerm]
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
