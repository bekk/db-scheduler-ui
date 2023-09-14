import { useQuery } from '@tanstack/react-query';
import { getLogs, LOG_QUERY_KEY } from 'src/services/getLogs';
import { Accordion, Box, HStack } from '@chakra-ui/react';
import React, { useState } from 'react';
import { Log } from 'src/models/Log';
import { LogCard } from 'src/components/history/LogCard';
import { useParams } from 'react-router-dom';
import colors from 'src/styles/colors';
import { HeaderBar } from '../HeaderBar';
import { FilterBy } from 'src/services/getTasks';

export const LogList: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState<string>('');

  const { taskName, taskInstance } = useParams();
  const { data } = useQuery([LOG_QUERY_KEY, taskName], () =>
    getLogs(taskName!, taskInstance!, searchTerm),
  );
  return (
    <Box>
      <HeaderBar
        title={'History' + (taskName ? ' for ' + taskName : '')}
        inputPlaceholder={`search for ${
          taskName ? '' : 'name, '
        }task id or execution id`}
        taskName={taskName || ''}
        currentFilter={FilterBy.All}
        setCurrentFilter={() => {}}
        setSearchTerm={setSearchTerm}
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
