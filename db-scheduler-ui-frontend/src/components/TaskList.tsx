import React from 'react';
import { Accordion, Box } from '@chakra-ui/react';
import { useParams } from 'react-router-dom';
import { isStatus } from 'src/utils/determineStatus';
import TaskCard from './TaskCard';
import TaskGroupCard from './TaskGroupCard';
import TitleRow from './TitleRow';
import { useInfiniteTaskScrolling } from 'src/hooks/useInfiniteTaskScrolling';
import { TASK_DETAILS_QUERY_KEY, getTask } from 'src/services/getTask';
import { TASK_QUERY_KEY, getTasks } from 'src/services/getTasks';
import { HeaderBar } from './HeaderBar';

const TaskList: React.FC = () => {
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
        inputPlaceholder={`search for ${isDetailsView ? '' : 'name or'}task id`}
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
