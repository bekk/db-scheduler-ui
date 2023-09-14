import { useQuery } from '@tanstack/react-query';
import { getLogs, LOG_QUERY_KEY } from 'src/services/getLogs';
import { Accordion, Box, HStack } from '@chakra-ui/react';
import React from 'react';
import { Log } from 'src/models/Log';
import { LogCard } from 'src/components/history/LogCard';
import { useParams } from 'react-router-dom';
import { LogInfoBox } from 'src/components/history/LogInfoBox';
import colors from 'src/styles/colors';
import { ALL_LOG_QUERY_KEY, getAllLogs } from 'src/services/getAllLogs';

export const LogList: React.FC = () => {
  const { taskName, taskInstance } = useParams();
  const { data } = useQuery(
    !taskName ? [LOG_QUERY_KEY, taskName] : [ALL_LOG_QUERY_KEY],
    () => (!taskName ? getAllLogs() : getLogs(taskName!, taskInstance!)),
  );
  return (
    <Box>
      <Box display={'flex'} mb={14}>
        <LogInfoBox taskName={taskName!} />
      </Box>
      <HStack
        display={'flex'}
        p="8px 16px"
        justifyContent={'space-around'}
        spacing={5}
        textColor={colors.primary['500']}
        fontSize={'sm'}
        textAlign="left"
      >
        <Box flex="1">Status</Box>
        <Box flex="2">Execution-ID</Box>
        <Box flex="2">Task-ID</Box>
        <Box flex="2">Last Execution</Box>
        <Box flex="2">Exception Class</Box>
        <Box flex="2">Exception Message</Box>
      </HStack>
      <Accordion allowMultiple>
        {data?.map((log: Log) => (
          <LogCard key={log.taskName + log.taskInstance} log={log} />
        ))}
      </Accordion>
    </Box>
  );
};
