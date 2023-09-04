import { AccordionButton, AccordionIcon, Box, HStack } from '@chakra-ui/react';
import { StatusBox } from 'src/components/StatusBox';
import { TaskRunButton } from 'src/components/TaskRunButton';
import React from 'react';
import { DotButton } from 'src/components/DotButton';
import { dateFormatText } from 'src/components/dateFormatText';
import { NumberCircle } from 'src/components/NumberCircle';

interface TaskAccordionButtonProps {
  taskName: string;
  executionTime: Date[];
  consecutiveFailures: number;
  picked: boolean;
  taskInstance: string[];
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
          <Box flex="2" textAlign="left" display={'flex'} alignItems={'center'}>
            {taskName}
            {taskInstance.length > 1 && (
              <Box ml={2}>
                <NumberCircle
                  number={taskInstance.length}
                  position={'relative'}
                  style={{
                    color: '#000000',
                  }}
                />
              </Box>
            )}
          </Box>
          <Box textAlign="left" flex="2">
            {(taskInstance[0].length > 20 // TODO: grey out ID but not ...more
              ? taskInstance[0].slice(0, 20) + '...'
              : taskInstance[0]) +
              (taskInstance.length > 1 ? ` + ${taskInstance.length} more` : '')}
          </Box>

          <HStack
            flex="2"
            textAlign="left"
            justifyContent={'flex-end'}
            flexDirection={'row'}
          >
            <Box flex={1}>{dateFormatText(new Date(executionTime[0]))}</Box>
            <TaskRunButton // Remove or adjust this button based on number of instances
              taskName={taskName}
              taskInstance={taskInstance[0]}
              picked={picked}
              consecutiveFailures={consecutiveFailures}
              refetch={refetch}
            />
            {!picked && (
              <DotButton // Remove or adjust this button based on number of instances
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
