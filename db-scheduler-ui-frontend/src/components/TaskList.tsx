import React, { useCallback, useEffect, useState } from 'react';

import { Accordion, Box, Text, IconButton } from '@chakra-ui/react';
import TaskCard from './TaskCard';
import {
  FilterBy,
  SortBy,
  TASK_QUERY_KEY,
  getTasks,
} from 'src/services/getTasks';

import { useQuery } from '@tanstack/react-query';
import PaginationButtons from 'src/components/PaginationButtons';
import { FilterBox } from 'src/components/FilterBox';
import TitleRow from 'src/components/TitleRow';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowBackIcon } from '@chakra-ui/icons';
import { TASK_DETAILS_QUERY_KEY, getTask } from 'src/services/getTask';
import TaskGroupCard from './TaskGroupCard';

const TaskList: React.FC = () => {
  const [currentFilter, setCurrentFilter] = useState<FilterBy>(FilterBy.All);
  const [currentSort, setCurrentSort] = useState<SortBy>(SortBy.Default);
  const [sortAsc, setSortAsc] = useState<boolean>(true);

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
        ]
      : [TASK_QUERY_KEY, currentFilter, page, currentSort, sortAsc],
    () =>
      isDetailsView
        ? getTask(
            currentFilter,
            { pageNumber: page - 1, limit: limit },
            currentSort,
            sortAsc,
            taskName,
          )
        : getTasks(
            currentFilter,
            { pageNumber: page - 1, limit: limit },
            currentSort,
            sortAsc,
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
      <Accordion allowMultiple>
        {data?.tasks.map((task) =>
          task.taskInstance.length === 1 ? (
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
