import { useQuery } from '@tanstack/react-query';
import { getLogs, LOG_QUERY_KEY } from 'src/services/getLogs';
import { Accordion, Box, HStack, Input } from '@chakra-ui/react';
import React, { useState } from 'react';
import { Log } from 'src/models/Log';
import { LogCard } from 'src/components/history/LogCard';
import { useParams } from 'react-router-dom';
import { LogInfoBox } from 'src/components/history/LogInfoBox';
import colors from 'src/styles/colors';

export const LogList: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState<string>('');

  const { taskName, taskInstance } = useParams();
  const { data } = useQuery([LOG_QUERY_KEY, taskName], () =>
    getLogs(taskName!, taskInstance!, searchTerm),
  );
  return (
    <Box>
      <Box display={'flex'} mb={14}>
        <LogInfoBox taskName={taskName!} />
      </Box>
      <Input
        placeholder={`search for task id or server id (if picked by)`}
        onChange={(e) => setSearchTerm(e.currentTarget.value)}
        bgColor={'white'}
        w={'30vmax'}
        mt={7}
      />
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
