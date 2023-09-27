/*
 * Copyright (C) Bekk
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
import React from 'react';
import { Accordion, Box, Button, Flex } from '@chakra-ui/react';
import { useParams } from 'react-router-dom';
import { isStatus } from 'src/utils/determineStatus';
import TaskCard from './TaskCard';
import TaskGroupCard from './TaskGroupCard';
import TitleRow from './TitleRow';
import { useInfiniteScrolling } from 'src/hooks/useInfiniteTaskScrolling';
import { TASK_DETAILS_QUERY_KEY, getTask } from 'src/services/getTask';
import { TASK_QUERY_KEY, getTasks } from 'src/services/getTasks';
import colors from 'src/styles/colors';
import { HeaderBar } from './HeaderBar';
import { TasksResponse } from 'src/models/TasksResponse';

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
    searchTermTaskName,
    searchTermTaskInstance,
    setSearchTermTaskName,
    setSearchTermTaskInstance,
    setTaskNameExactMatch,
    setTaskInstanceExactMatch,
  } = useInfiniteScrolling<TasksResponse>(
    isDetailsView
      ? {
          fetchDataFunction: getTask,
          taskName: taskName,
          baseQueryKey: TASK_DETAILS_QUERY_KEY,
        }
      : { fetchDataFunction: getTasks, baseQueryKey: TASK_QUERY_KEY },
  );

  return (
    <Box>
      <HeaderBar
        title={isDetailsView ? taskName : 'All Tasks'}
        taskName={taskName || ''}
        taskInstance={''}
        currentFilter={currentFilter}
        searchTerm={searchTerm}
        asc={sortAsc}
        setCurrentFilter={setCurrentFilter}
        setSearchTermTaskName={setSearchTermTaskName}
        setSearchTermTaskInstance={setSearchTermTaskInstance}
        setTaskNameExactMatch={setTaskNameExactMatch}
        setTaskInstanceExactMatch={setTaskInstanceExactMatch}
        searchTermTaskName={searchTermTaskName}
        searchTermTaskInstance={searchTermTaskInstance}
        refetch={refetch}
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
          p.items.map((task) =>
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
