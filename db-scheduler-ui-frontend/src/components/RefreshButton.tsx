import { Box, Button } from '@chakra-ui/react';
import { RepeatIcon } from '@chakra-ui/icons';
import React, { useEffect, useState } from 'react';
import colors from 'src/styles/colors';
import { useQuery } from '@tanstack/react-query';
import { POLL_TASKS_QUERY_KEY, pollTasks } from 'src/services/pollTasks';
import { TaskDetailsRequestParams } from 'src/models/TaskRequestParams';
import { NumberCircle } from './NumberCircle';

interface RefreshButtonProps {
  refetch: () => void;
  params: TaskDetailsRequestParams;
  isFetched: boolean;
}

export const RefreshButton: React.FC<RefreshButtonProps> = ({
  refetch,
  params,
  isFetched,
}) => {
  const { data, fetchStatus } = useQuery(
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

  useEffect(() => {
    console.log('isFetched', isFetched, fetchStatus);
    isFetched && refetch();
  }, [isFetched, refetch]);

  return (
    <Box position="relative" display="inline-block">
      <Button
        onClick={() => {
          refetch();
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

type RefreshCircleProps = {
  number: number;
  color: string;
  textColor?: string;
  visible?: boolean;
  hoverText: string;
};

const RefreshCircle: React.FC<RefreshCircleProps> = ({
  number,
  color,
  textColor,
  visible,
  hoverText,
}) => {
  const [hovered, setHovered] = useState(false);

  const text = hovered ? hoverText : '';

  const powerOfTen = (number + hoverText).length - 1; // TODO: Fix this
  const isExpanded = 1 <= powerOfTen;
  const baseSize: number = 22;
  const width = isExpanded ? baseSize + 7 * powerOfTen : baseSize;

  const marginLeft = isExpanded ? -width : 0;

  return (
    <Box
      alignItems={'end'}
      display={'flex'}
      justifyContent={'flex-end'}
      overflow={'visible'}
      ml={marginLeft}
      position="relative"
      visibility={visible ? 'visible' : 'hidden'}
    >
      <Box
        onMouseEnter={() => setHovered(true)}
        onMouseLeave={() => setHovered(false)}
      >
        <NumberCircle
          number={number + text}
          bgColor={color}
          textColor={textColor}
          position="relative"
          top={'auto'}
          style={{ bottom: '0', left: '0' }}
        />
      </Box>
    </Box>
  );
};
