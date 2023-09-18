import React from 'react';
import { Accordion, IconButton, Box, Text } from '@chakra-ui/react';
import { useParams, useNavigate } from 'react-router-dom';
import { isStatus } from 'src/utils/determineStatus';
import TaskCard from './TaskCard';
import TaskGroupCard from './TaskGroupCard';
import { ArrowBackIcon } from '@chakra-ui/icons';
import { FilterBox } from './FilterBox';
import TitleRow from './TitleRow';
import { useInfiniteTaskScrolling } from 'src/hooks/useInfiniteTaskScrolling';
import { TASK_DETAILS_QUERY_KEY, getTask } from 'src/services/getTask';
import { TASK_QUERY_KEY, getTasks } from 'src/services/getTasks';

const TaskList: React.FC = () => {
  const navigate = useNavigate();
  const { taskName } = useParams<{ taskName?: string }>();
  const isDetailsView = !!taskName;

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    refetch,
    currentFilter,
    setCurrentFilter,
    currentSort,
    setCurrentSort,
    sortAsc,
    setSortAsc,
  } = useInfiniteTaskScrolling(
    isDetailsView
      ? {
          getTasksFunction: getTask,
          taskName: taskName,
          baseQueryKey: TASK_DETAILS_QUERY_KEY,
        }
      : { getTasksFunction: getTasks, baseQueryKey: TASK_QUERY_KEY },
  );

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
