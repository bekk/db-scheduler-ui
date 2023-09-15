import React, { useCallback, useEffect, useState } from 'react';
import { useInfiniteQuery } from '@tanstack/react-query';
import { Accordion, IconButton, Box, Text } from '@chakra-ui/react';
import { useParams, useNavigate } from 'react-router-dom';
import { getTask, TASK_DETAILS_QUERY_KEY } from 'src/services/getTask';
import {
  FilterBy,
  SortBy,
  getTasks,
  TASK_QUERY_KEY,
} from 'src/services/getTasks';
import { isStatus } from 'src/utils/determineStatus';
import TaskCard from './TaskCard';
import TaskGroupCard from './TaskGroupCard';
import { ArrowBackIcon } from '@chakra-ui/icons';
import { FilterBox } from './FilterBox';
import TitleRow from './TitleRow';

const TaskList: React.FC = () => {
  const [currentFilter, setCurrentFilter] = useState<FilterBy>(FilterBy.All);
  const [currentSort, setCurrentSort] = useState<SortBy>(SortBy.Default);
  const [sortAsc, setSortAsc] = useState<boolean>(true);

  const { taskName } = useParams<{ taskName?: string }>();
  const navigate = useNavigate();
  const limit = 10;
  const isDetailsView = !!taskName;
  const [refetchInterval, setRefetchInterval] = useState<number | false>(2000);

  // TODO: Make a hook for the infinite scroll stuff
  // TODO: Clear when switching between filters (now it reappears when you switch back to the original filter)
  const fetchTasks = useCallback(
    ({ pageParam = 0 }) => {
      return isDetailsView
        ? getTask(
            currentFilter,
            { pageNumber: pageParam, limit: limit },
            currentSort,
            sortAsc,
            pageParam === 0 ? true : false,
            taskName,
          )
        : getTasks(
            currentFilter,
            { pageNumber: pageParam, limit: limit },
            currentSort,
            sortAsc,
            pageParam === 0 ? true : false,
          );
    },
    [currentFilter, currentSort, sortAsc, taskName, isDetailsView],
  );

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, refetch } =
    useInfiniteQuery(
      isDetailsView
        ? [
            TASK_DETAILS_QUERY_KEY,
            currentFilter,
            currentSort,
            sortAsc,
            taskName,
          ]
        : [TASK_QUERY_KEY, currentFilter, currentSort, sortAsc],
      fetchTasks,
      {
        getNextPageParam: (lastPage, allPages) => {
          const nextPage = allPages.length + 1;
          return nextPage <= lastPage.numberOfPages ? nextPage : undefined;
        },
        refetchInterval: refetchInterval,
      },
    );

  useEffect(() => {
    if ((data?.pages?.length || 0) > 1) {
      setRefetchInterval(false);
    } else {
      setRefetchInterval(2000);
    }
  }, [data]);

  return (
    <Box>
      <Box display={'flex'} mb={14} alignItems={'center'}>
        {isDetailsView && (
          <IconButton
            icon={<ArrowBackIcon boxSize={8} />}
            onClick={() => navigate('/')}
            aria-label={'Back button'}
            variant={'ghost'}
            isRound
          />
        )}
        <Text ml={5} fontSize={'3xl'} fontWeight={'semibold'}>
          {isDetailsView ? taskName : 'All Tasks'}
        </Text>
        <FilterBox
          currentFilter={currentFilter}
          setCurrentFilter={setCurrentFilter}
        />
      </Box>
      <TitleRow
        currentSort={currentSort}
        setCurrentSort={setCurrentSort}
        sortAsc={sortAsc}
        setSortAsc={setSortAsc}
        isDetailsView={isDetailsView}
      />
      <Accordion allowMultiple key={taskName || 'all'}>
        {data?.pages.map((p) =>
          p.tasks.map((task) =>
            !isStatus('Group', task) ? (
              <TaskCard
                key={task.taskInstance + task.taskName}
                {...task}
                refetch={refetch}
              />
            ) : (
              <TaskGroupCard key={task.taskName} {...task} refetch={refetch} />
            ),
          ),
        )}
      </Accordion>
      <button
        onClick={() => fetchNextPage()}
        disabled={!hasNextPage || isFetchingNextPage}
      >
        {isFetchingNextPage
          ? 'Loading more...'
          : hasNextPage
          ? 'Load More'
          : 'Nothing more to load'}
      </button>
    </Box>
  );
};

export default TaskList;
