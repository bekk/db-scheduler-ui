import { Accordion, Box, Button, Flex, HStack, Text } from '@chakra-ui/react';
import React from 'react';
import { LogCard } from 'src/components/history/LogCard';
import { useParams } from 'react-router-dom';
import colors from 'src/styles/colors';
import { HeaderBar } from '../HeaderBar';
import { ALL_LOG_QUERY_KEY, getLogs } from 'src/services/getLogs';
import { useInfiniteScrolling } from 'src/hooks/useInfiniteTaskScrolling';
import { DateTimeInput } from 'src/components/history/DateTimeInput';
import { SortButton } from 'src/components/SortButton';
import { SortBy } from 'src/models/QueryParams';
import { LogResponse } from 'src/models/TasksResponse';

export const LogList: React.FC = () => {
  const { taskName, taskInstance } = useParams();
  const [startTime, setStartTime] = React.useState<Date | null>(null);
  const [endTime, setEndTime] = React.useState<Date | null>(null);
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

  return (
    <Box>
      <HeaderBar
        title={'History' + (taskName ? ' for ' + taskName : '')}
        inputPlaceholder={`search for ${
          taskName ? '' : 'task name or '
        }task id`}
        taskName={taskName || ''}
        currentFilter={currentFilter}
        setCurrentFilter={setCurrentFilter}
        setSearchTerm={setSearchTerm}
        refetch={refetch}
        history
      />
      <Box mb={14}>
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
