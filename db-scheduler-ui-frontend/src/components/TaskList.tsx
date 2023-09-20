import React from 'react';
import { Accordion, Box, Button, Flex } from '@chakra-ui/react';
import { useParams } from 'react-router-dom';
import { isStatus } from 'src/utils/determineStatus';
import TaskCard from './TaskCard';
import TaskGroupCard from './TaskGroupCard';
import TitleRow from './TitleRow';
import { useInfiniteTaskScrolling } from 'src/hooks/useInfiniteTaskScrolling';
import { TASK_DETAILS_QUERY_KEY, getTask } from 'src/services/getTask';
import { TASK_QUERY_KEY, getTasks } from 'src/services/getTasks';
import { HeaderBar } from './HeaderBar';
import colors from 'src/styles/colors';

const TaskList: React.FC = () => {
  const { taskName } = useParams<{ taskName?: string }>();
  const isDetailsView = !!taskName;

  // TODO: Make no-polling default
  // TODO: Add a way to force refresh
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
    setSearchTerm,
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
      <HeaderBar
        title={isDetailsView ? taskName : 'All Tasks'}
        inputPlaceholder={`search for ${
          isDetailsView ? '' : 'name or '
        }task id`}
        taskName={taskName || ''}
        currentFilter={currentFilter}
        setCurrentFilter={setCurrentFilter}
        setSearchTerm={setSearchTerm}
      />

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
      <Flex justifyContent="center" alignItems="center" mt={4}>
        <Button
          onClick={() => fetchNextPage()}
          bgColor={'white'}
          isDisabled={!hasNextPage || isFetchingNextPage}
          borderColor={colors.primary}
          borderWidth={1}
          fontWeight={'medium'}
          mb={24}
        >
          {isFetchingNextPage
            ? 'Loading...'
            : hasNextPage
            ? 'Load More'
            : 'Nothing more to load'}
        </Button>
      </Flex>
    </Box>
  );
};

export default TaskList;
