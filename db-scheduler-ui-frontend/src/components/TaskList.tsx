import React, { useCallback, useEffect, useState } from 'react';

import { Accordion, Box } from '@chakra-ui/react';
import TaskCard from './TaskCard';
import {
  FilterBy,
  SortBy,
  TASK_QUERY_KEY,
  getTasks,
} from 'src/services/getTasks';

import { useQuery } from '@tanstack/react-query';
import PaginationButtons from 'src/components/PaginationButtons';
import TitleRow from 'src/components/TitleRow';
import { useNavigate, useParams } from 'react-router-dom';
import { TASK_DETAILS_QUERY_KEY, getTask } from 'src/services/getTask';
import TaskGroupCard from './TaskGroupCard';
import { isStatus } from 'src/utils/determineStatus';
import { HeaderBar } from './HeaderBar';

const TaskList: React.FC = () => {
  const [currentFilter, setCurrentFilter] = useState<FilterBy>(FilterBy.All);
  const [currentSort, setCurrentSort] = useState<SortBy>(SortBy.Default);
  const [sortAsc, setSortAsc] = useState<boolean>(true);
  const [searchTerm, setSearchTerm] = useState<string>('');

  const { taskName, page: rawPage } = useParams<{
    taskName?: string;
    page?: string;
  }>();
  const page = Number(rawPage) && Number(rawPage) > 0 ? Number(rawPage) : 1;

  const limit = 10;
  const isDetailsView = !!taskName;
  const { data, refetch } = useQuery(
    isDetailsView
      ? [
          TASK_DETAILS_QUERY_KEY,
          currentFilter,
          page,
          currentSort,
          sortAsc,
          taskName,
          searchTerm,
        ]
      : [TASK_QUERY_KEY, currentFilter, page, currentSort, sortAsc, searchTerm],
    () =>
      isDetailsView
        ? getTask(
            currentFilter,
            { pageNumber: page - 1, limit: limit },
            currentSort,
            sortAsc,
            searchTerm,
            taskName,
          )
        : getTasks(
            currentFilter,
            { pageNumber: page - 1, limit: limit },
            currentSort,
            sortAsc,
            searchTerm,
          ),
  );
  const navigate = useNavigate();

  const setPage = useCallback(
    (page: number) => {
      navigate(`/${taskName ? taskName + '/' : ''}/page/${page}`);
    },
    [navigate, taskName],
  );

  useEffect(() => {
    setSortAsc(true);
  }, [currentSort]);

  useEffect(() => {
    if (data?.numberOfPages && page > data.numberOfPages) {
      setPage(data.numberOfPages);
    }
  }, [data, page, setPage]);

  return (
    <Box>
      <HeaderBar
        title={isDetailsView ? taskName : 'All Tasks'}
        inputPlaceholder={`search for ${
          isDetailsView ? '' : 'name, '
        }task id or server id (if picked by)`}
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
        {data?.tasks.map((task) =>
          !isStatus('Group', task) ? (
            <TaskCard
              key={task.taskInstance + task.taskName}
              {...task}
              refetch={refetch}
            />
          ) : (
            <TaskGroupCard key={task.taskName} {...task} refetch={refetch} />
          ),
        )}
      </Accordion>
      <PaginationButtons
        page={page}
        limit={10}
        setPage={setPage}
        numberOfPages={data?.numberOfPages}
      />
    </Box>
  );
};

export default TaskList;
