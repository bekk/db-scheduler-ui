import {
  AccordionButton,
  AccordionIcon,
  Box,
  Flex,
  HStack,
  Text,
} from '@chakra-ui/react';
import { StatusBox } from 'src/components/StatusBox';
import { TaskRunButton } from 'src/components/TaskRunButton';
import React from 'react';
import { DotButton } from 'src/components/DotButton';
import { dateFormatText } from 'src/components/dateFormatText';
import { NumberCircle } from 'src/components/NumberCircle';
import { useParams } from 'react-router-dom';

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
  const { taskName: isDetailsView } = useParams<{ taskName?: string }>();
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
          {!isDetailsView && (
            <Box
              flex="2"
              textAlign="left"
              display={'flex'}
              alignItems={'center'}
            >
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
          )}
          <Flex wrap={'wrap'} textAlign="left" flex="2" display={'flex'}>
            <Text>{taskInstance[0].slice(0, 20)}</Text>
            {taskInstance[0].length > 20 && (
              <Text color={'#555555'}>
                {taskInstance.length > 1
                  ? `... + ${taskInstance.length - 1} more`
                  : ''}
              </Text>
            )}
          </Flex>

          <HStack
            flex="2"
            textAlign="left"
            justifyContent={'flex-end'}
            flexDirection={'row'}
          >
            <Box flex={1}>{dateFormatText(new Date(executionTime[0]))}</Box>
            <Box display={'flex'} justifyContent={'space-between'} w={150}>
              <TaskRunButton
                taskName={taskName}
                taskInstance={taskInstance}
                picked={picked}
                consecutiveFailures={consecutiveFailures}
                refetch={refetch}
              />
              {!picked && taskInstance.length === 1 && (
                <DotButton
                  taskName={taskName}
                  taskInstance={taskInstance[0]}
                  style={{ marginRight: 5 }}
                />
              )}
            </Box>
            <AccordionIcon />
          </HStack>
        </HStack>
      </AccordionButton>
    </h2>
  );
};
