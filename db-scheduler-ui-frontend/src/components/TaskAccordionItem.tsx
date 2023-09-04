import { AccordionPanel, Box, VStack } from '@chakra-ui/react';
import { TaskDataRow } from 'src/components/TaskDataRow';
import React from 'react';

interface TaskAccordionItemProps {
  lastSuccess: Date | null;
  lastFailure: Date | null;
  taskData: string | null;
}

export const TaskAccordionItem: React.FC<TaskAccordionItemProps> = ({
  lastFailure,
  lastSuccess,
  taskData,
}) => (
  <AccordionPanel pb={4}>
    <Box display="flex" justifyContent={'space-between'}>
      <VStack
        align="start"
        spacing={2}
        bgColor={'#FFFFFF'}
        p={2}
        w={'100%'}
        borderRadius={4}
      >
        {(lastSuccess || lastFailure) && (
          <Box>
            <strong>Last Execution Time: </strong>
            {lastSuccess
              ? new Date(lastSuccess)?.toLocaleString()
              : lastFailure && new Date(lastFailure)?.toLocaleString()}
          </Box>
        )}
        <TaskDataRow taskData={taskData} />
      </VStack>
    </Box>
  </AccordionPanel>
);
