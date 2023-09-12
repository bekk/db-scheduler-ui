import { AccordionPanel, Box, VStack } from '@chakra-ui/react';
import { TaskDataRow } from 'src/components/TaskDataRow';
import React from 'react';
import { dateFormatText } from 'src/utils/dateFormatText';

interface TaskAccordionItemProps {
  lastSuccess: Date | null;
  lastFailure: Date | null;
  taskData: (object | null)[];
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
              ? dateFormatText(new Date(lastSuccess))
              : lastFailure && dateFormatText(new Date(lastFailure))}
          </Box>
        )}
        <TaskDataRow taskData={taskData} />
      </VStack>
    </Box>
  </AccordionPanel>
);
