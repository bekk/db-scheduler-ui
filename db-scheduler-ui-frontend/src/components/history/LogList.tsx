import { useQuery } from '@tanstack/react-query';
import { getLogs, LOG_QUERY_KEY } from 'src/services/getLogs';
import { Accordion, Box, HStack } from '@chakra-ui/react';
import React from 'react';
import { Log } from 'src/models/Log';
import { LogCard } from 'src/components/history/LogCard';
import { useParams } from 'react-router-dom';
import { LogInfoBox } from 'src/components/history/LogInfoBox';

export const LogList: React.FC = () => {
  const { taskName, taskInstance } = useParams();
  const { data } = useQuery([LOG_QUERY_KEY, taskName], () =>
    getLogs(taskName!, taskInstance!),
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
      >
        <Box flex="1" textAlign="left" textColor={'#484848'} fontSize={'sm'}>
          Status
        </Box>
        <Box flex="2" textAlign="left" textColor={'#484848'} fontSize={'sm'}>
          Execution-ID
        </Box>
        <Box flex="2" textAlign="left" textColor={'#484848'} fontSize={'sm'}>
          Task-ID
        </Box>
        <Box flex="2" textAlign="left" textColor={'#484848'} fontSize={'sm'}>
          Last Execution
        </Box>
        <Box flex="2" textAlign="left" textColor={'#484848'} fontSize={'sm'}>
          Exception Class
        </Box>
        <Box flex="2" textAlign="left" textColor={'#484848'} fontSize={'sm'}>
          Exception Message
        </Box>
      </HStack>
      <Accordion allowMultiple>
        {data?.map((log: Log) => (
          <LogCard key={log.taskName + log.taskInstance} log={log} />
        ))}
      </Accordion>
    </Box>
  );
};
