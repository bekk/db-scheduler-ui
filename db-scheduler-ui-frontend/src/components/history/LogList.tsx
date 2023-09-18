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
import { ALL_LOG_QUERY_KEY, getAllLogs } from 'src/services/getAllLogs';

export const LogList: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [currentFilter, setCurrentFilter] = useState<FilterBy>(FilterBy.All);

  const { taskName, taskInstance } = useParams();
  const { data } = useQuery(
    !taskName
      ? [LOG_QUERY_KEY, taskName, currentFilter, searchTerm]
      : [ALL_LOG_QUERY_KEY, currentFilter, searchTerm],
    () =>
      !taskName
        ? getAllLogs(currentFilter, searchTerm)
        : getLogs(taskName!, taskInstance!, currentFilter, searchTerm),
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
        history
      />
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
        <Box flex="2" hidden={!!taskName}>
          Task Name
        </Box>
        <Box flex="2">Task-ID</Box>
        <Box flex="2">Time Finished</Box>
        <Box flex="2">Exception Message</Box>
        <Box flex="0.2" />
      </HStack>
      <Accordion allowMultiple>
        {data?.map((log: Log) => (
          <LogCard key={log.taskName + log.taskInstance + log.id} log={log} />
        ))}
      </Accordion>
    </Box>
  );
};
