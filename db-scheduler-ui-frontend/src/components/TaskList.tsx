import React, { useEffect, useState } from 'react';

import { Accordion, Box } from '@chakra-ui/react';
import TaskCard from './TaskCard';
import {
  FilterBy,
  PaginationParams,
  SortBy,
  TASK_QUERY_KEY,
  getTasks,
} from 'src/services/getTasks';

import { useQuery } from '@tanstack/react-query';
import PaginationButtons from 'src/components/PaginationButtons';
import { FilterBox } from 'src/components/FilterBox';
import TitleRow from 'src/components/TitleRow';

const TaskList: React.FC = () => {
  const [currentFilter, setCurrentFilter] = useState<FilterBy>(FilterBy.All);
  const [page, setPage] = useState<PaginationParams>({
    limit: 10,
    pageNumber: 0,
  });
  const [currentSort, setCurrentSort] = useState<SortBy>(SortBy.Default);
  const [sortAsc, setSortAsc] = useState<boolean>(true);
  const { data, refetch } = useQuery(
    [TASK_QUERY_KEY, currentFilter, page, currentSort, sortAsc],
    () => getTasks(currentFilter, page, currentSort, sortAsc),
  );

  useEffect(() => {
    setSortAsc(true);
  }, [currentSort]);

  useEffect(() => {
    if (data?.numberOfPages && page.pageNumber + 1 > data?.numberOfPages) {
      setPage((prev) => {
        return { ...prev, pageNumber: data?.numberOfPages - 1 };
      });
    }
  }, [data, page]);

  return (
    <Box>
      <Box display={'flex'} mb={14}>
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
      />
      <Accordion allowMultiple>
        {data?.tasks?.map((task) => (
          <TaskCard
            key={task.taskInstance + task.taskName}
            {...task}
            refetch={refetch}
          />
        ))}
      </Accordion>
      <PaginationButtons
        page={page}
        setPage={setPage}
        numberOfPages={data?.numberOfPages}
      />
    </Box>
  );
};

export default TaskList;
