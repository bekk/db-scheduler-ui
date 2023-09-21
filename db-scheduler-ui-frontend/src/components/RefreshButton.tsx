import { Box, Button } from '@chakra-ui/react';
import { RepeatIcon } from '@chakra-ui/icons';
import React from 'react';
import colors from 'src/styles/colors';
import {
  InfiniteData,
  QueryObserverResult,
  useQuery,
} from '@tanstack/react-query';
import { POLL_TASKS_QUERY_KEY, pollTasks } from 'src/services/pollTasks';
import { TaskDetailsRequestParams } from 'src/models/TaskRequestParams';
import { TasksResponse } from 'src/models/TasksResponse';
import { RefreshCircle } from './RefreshCircle';

interface RefreshButtonProps {
  refetch?: () => Promise<
    QueryObserverResult<InfiniteData<TasksResponse>, unknown>
  >;
  params: TaskDetailsRequestParams;
}

export const RefreshButton: React.FC<RefreshButtonProps> = ({
  refetch,
  params,
}) => {
  const { data, refetch: repoll } = useQuery(
    [
      POLL_TASKS_QUERY_KEY,
      params.filter,
      params.sorting,
      params.asc,
      params.startTime,
      params.endTime,
      params.taskName,
      params.taskId,
      params.searchTerm,
    ],
    () =>
      pollTasks({
        filter: params.filter,
        sorting: params.sorting,
        asc: params.asc,
        startTime: params.startTime,
        endTime: params.endTime,
        taskName: params.taskName,
        taskId: params.taskId,
        searchTerm: params.searchTerm,
      }),
  );

  return (
    <Box position="relative" display="inline-block">
      <Button
        onClick={() => {
          refetch && refetch().then(() => repoll());
        }}
        iconSpacing={3}
        p={4}
        py={7}
        borderColor={colors.primary['300']}
        borderWidth={1}
        bgColor={colors.primary['100']}
        fontWeight="normal"
        rightIcon={<RepeatIcon boxSize={'7'} />}
      >
        <Box textAlign={'left'}>Refresh</Box>
      </Button>
      <Box
        pos="absolute"
        left={2}
        justifyContent={'flex-end'}
        top={-1}
        display="flex"
        flexDirection="column"
      >
        <RefreshCircle
          number={data?.newFailures ?? 0}
          color={colors.failed['200']}
          visible={data?.newFailures !== 0}
          hoverText=" failed since refresh"
        />
        <RefreshCircle
          number={data?.newRunning ?? 0}
          color={colors.running['300']}
          visible={data?.newRunning !== 0}
          hoverText=" running since refresh"
        />
        <RefreshCircle
          number={data?.newTasks ?? 0}
          color={colors.primary['300']}
          textColor={colors.primary['900']}
          visible={data?.newTasks !== 0}
          hoverText=" added since refresh"
        />
      </Box>
    </Box>
  );
};
