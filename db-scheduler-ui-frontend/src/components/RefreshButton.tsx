import { Box, Button } from '@chakra-ui/react';
import { RepeatIcon } from '@chakra-ui/icons';
import React from 'react';
import colors from 'src/styles/colors';
import { useQuery } from '@tanstack/react-query';
import { POLL_TASKS_QUERY_KEY, pollTasks } from 'src/services/pollTasks';
import { TaskDetailsRequestParams } from 'src/models/TaskRequestParams';
import { NumberCircle } from './NumberCircle';

interface RefreshButtonProps {
  refetch: () => void;
  params: TaskDetailsRequestParams;
}

export const RefreshButton: React.FC<RefreshButtonProps> = ({
  refetch,
  params,
}) => {
  const { data } = useQuery(
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
  console.log(data);
  const testData = {
    newFailures: 1,
    newRunning: 22,
    newTasks: 333,
  };

  return (
    <Box position="relative" display="inline-block">
      <Button
        onClick={(event) => {
          console.log('event', event);
          refetch();
        }}
        iconSpacing={3}
        width={'auto'}
        height={'100'}
        transition="all 0.8s"
        p={3}
        bgColor={colors.primary['100']}
        fontWeight="normal"
        rightIcon={<RepeatIcon boxSize={'7'} />}
        _hover={{
          height: '200',
          '.circleContainer': { transform: 'rotate(0.25turn)' },
          '.singleCircle': {
            transform: 'rotate(-0.25turn)',
            transition: 'all 0.8s ease-in-out',
          },
        }}
        animation={''}
      >
        <Box textAlign={'left'}>
          Refresh
          <Box
            mt={2}
            className="circleContainer"
            display="flex"
            flexDirection="row"
            transition="all 0.8s ease-in-out"
          >
            {testData?.newFailures && (
              <RefreshCircle number={testData?.newFailures} color={'red'} />
            )}
            {testData?.newRunning && (
              <RefreshCircle number={testData?.newRunning} color={'blue'} />
            )}
            {testData?.newTasks && (
              <RefreshCircle number={testData?.newTasks} color={'grey'} />
            )}
          </Box>
        </Box>
      </Button>
    </Box>
  );
};

type RefreshCircleProps = {
  number: number;
  color: string;
  transform?: string;
};

const RefreshCircle: React.FC<RefreshCircleProps> = ({ number, color }) => {
  return (
    <Box className="singleCircle">
      <NumberCircle
        number={number}
        bgColor={color}
        position="relative"
        top={'auto'}
        style={{ bottom: '0', left: '0' }}
      />
    </Box>
  );
};
