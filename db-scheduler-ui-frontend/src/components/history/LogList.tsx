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
import { Accordion, Box, Button, Flex, HStack, Text } from '@chakra-ui/react';
import React, { useEffect } from 'react';
import { LogCard } from 'src/components/history/LogCard';
import { useLocation } from 'react-router-dom';
import colors from 'src/styles/colors';
import { HeaderBar } from '../common/HeaderBar';
import { ALL_LOG_QUERY_KEY, getLogs } from 'src/services/getLogs';
import { useInfiniteScrolling } from 'src/hooks/useInfiniteTaskScrolling';
import { DateTimeInput } from 'src/components/input/DateTimeInput';
import { SortButton } from 'src/components/input/SortButton';
import { SortBy } from 'src/models/QueryParams';
import { LogResponse } from 'src/models/TasksResponse';

export const LogList: React.FC = () => {
  const location = useLocation();
  const { taskName, taskInstance } = location.state || {};

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
    startTime,
    setStartTime,
    endTime,
    setEndTime,
    setSearchTermTaskName,
    setSearchTermTaskInstance,
    searchTermTaskName,
    searchTermTaskInstance,
    taskInstanceExactMatch,
    taskNameExactMatch,
    setTaskNameExactMatch,
    setTaskInstanceExactMatch,
  } = useInfiniteScrolling<LogResponse>(
    taskName
      ? {
          fetchDataFunction: getLogs,
          taskName: taskName,
          taskInstance: taskInstance,
          baseQueryKey: ALL_LOG_QUERY_KEY,
        }
      : { fetchDataFunction: getLogs, baseQueryKey: ALL_LOG_QUERY_KEY },
  );

  useEffect(() => {
    if (taskName || taskInstance) {
      setSearchTermTaskName(taskName || '');
      setSearchTermTaskInstance(taskInstance || '');
    }
  }, [
    setSearchTermTaskInstance,
    setSearchTermTaskName,
    taskInstance,
    taskName,
  ]);

  return (
    <Box>
      <HeaderBar
        title={'History' + (taskName ? ' for ' + taskName : '')}
        params={{
          filter: currentFilter,
          asc: sortAsc,
          startTime: startTime ?? undefined,
          endTime: endTime ?? undefined,
          taskName,
          taskId: taskInstance,
          searchTermTaskName,
          searchTermTaskInstance,
          taskInstanceExactMatch,
          taskNameExactMatch,
        }}
        setCurrentFilter={setCurrentFilter}
        setSearchTermTaskName={setSearchTermTaskName}
        setSearchTermTaskInstance={setSearchTermTaskInstance}
        refetch={refetch}
        setTaskNameExactMatch={setTaskNameExactMatch}
        setTaskInstanceExactMatch={setTaskInstanceExactMatch}
        history
      />
      <Box mb={7}>
        <Flex alignItems={'center'}>
          <DateTimeInput
            selectedDate={startTime}
            onChange={(date) => {
              setStartTime(date);
            }}
          />
          <Text mx={3}>-</Text>
          <DateTimeInput
            selectedDate={endTime}
            onChange={(date) => setEndTime(date)}
          />
        </Flex>
      </Box>
      <HStack
        display={'flex'}
        p="8px 16px"
        justifyContent={'space-between'}
        spacing={5}
        textColor={colors.primary['500']}
        fontSize={'sm'}
        textAlign="left"
      >
        <Box flex="1">Status</Box>
        <Box flex={2}>Task Name</Box>
        <Box flex="2">Task-ID</Box>
        <SortButton
          currentSort={currentSort}
          setCurrentSort={setCurrentSort}
          sortAsc={sortAsc}
          setSortAsc={setSortAsc}
          title={'Time Finished'}
          name={SortBy.Default}
        ></SortButton>
        <Box flex="2">Exception Message</Box>
        <Box flex="0.2" />
      </HStack>
      <Accordion allowMultiple>
        {data?.pages.map((p) =>
          p.items.map((log) => (
            <LogCard key={log.id + log.taskName + log.taskInstance} log={log} />
          )),
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
