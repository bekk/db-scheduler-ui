import { AccordionButton, AccordionIcon, Box, HStack } from '@chakra-ui/react';
import { StatusBox } from 'src/components/StatusBox';
import { TaskRunButton } from 'src/components/TaskRunButton';
import React from 'react';
import { DotButton } from 'src/components/DotButton';
import { dateFormatText } from 'src/components/dateFormatText';

interface TaskAccordionButtonProps {
  taskName: string;
  executionTime: Date;
  consecutiveFailures: number;
  picked: boolean;
  taskInstance: string;
  refetch: () => void;
}

const status = ['Failed', 'Running', 'Scheduled'];
export const TaskAccordionButton: React.FC<TaskAccordionButtonProps> = ({
  taskName,
  executionTime,
  consecutiveFailures,
  picked,
  taskInstance,
  refetch,
}) => {
  return (
    <h2>
      <AccordionButton _hover={{ backgroundColor: '#FFFFFF' }}>
        <HStack w={'100%'} spacing={5}>
          <Box flex="1" display="inline-flex">
            <StatusBox
              status={
                picked
                  ? status[1]
                  : consecutiveFailures > 0
                  ? status[0]
                  : status[2]
              }
              consecutiveFailures={consecutiveFailures}
            />
          </Box>
          <Box flex="2" textAlign="left">
            {taskName}
          </Box>
          <Box textAlign="left" flex="2">
            {taskInstance}
          </Box>

          <HStack
            flex="2"
            textAlign="left"
            justifyContent={'flex-end'}
            flexDirection={'row'}
          >
            <Box flex={1}>{dateFormatText(new Date(executionTime))}</Box>
            {/*<DateFormatText executionDate={new Date(executionTime)} />*/}
            <TaskRunButton
              taskName={taskName}
              taskInstance={taskInstance}
              picked={picked}
              consecutiveFailures={consecutiveFailures}
              refetch={refetch}
            />
            {!picked && (
              <DotButton
                taskName={taskName}
                taskInstance={taskInstance}
                style={{ marginRight: 5 }}
              />
            )}
            <AccordionIcon />
          </HStack>
        </HStack>
      </AccordionButton>
    </h2>
  );
};
