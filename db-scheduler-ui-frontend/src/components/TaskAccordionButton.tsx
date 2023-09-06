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
import { dateFormatText } from 'src/utils/dateFormatText';
import { useParams } from 'react-router-dom';
import { Task } from 'src/models/Task';
import { NumberCircleGroup } from './NumberCircleGroup';

interface TaskAccordionButtonProps extends Task {
  refetch: () => void;
}

const status = ['Failed', 'Running', 'Scheduled', 'Group'];
export const TaskAccordionButton: React.FC<TaskAccordionButtonProps> = (
  props,
) => {
  const {
    taskName,
    executionTime,
    consecutiveFailures,
    picked,
    taskInstance,
    refetch,
  } = props;
  const { taskName: isDetailsView } = useParams<{ taskName?: string }>();
  return (
    <h2>
      <AccordionButton _hover={{ backgroundColor: '#FFFFFF' }}>
        <HStack w={'100%'} spacing={5}>
          <Box flex="1" display="inline-flex">
            <StatusBox
              status={
                taskInstance.length > 1
                  ? status[3]
                  : picked
                  ? status[1]
                  : consecutiveFailures[0] > 0
                  ? status[0]
                  : status[2]
              }
              consecutiveFailures={consecutiveFailures[0]}
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
                  <NumberCircleGroup {...props} />
                </Box>
              )}
            </Box>
          )}
          <Flex wrap={'wrap'} textAlign="left" flex="2" display={'flex'}>
            <Text>
              {taskInstance[0].length > 40
                ? taskInstance[0].slice(0, 40) + '...'
                : taskInstance[0]}
            </Text>
            {taskInstance.length > 1 && (
              <Text color={'#555555'}>
                {taskInstance.length > 1
                  ? ` + ${taskInstance.length - 1} more`
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
            <Box
              display={'flex'}
              justifyContent={
                taskInstance.length === 1 ? 'space-between' : 'end'
              }
              w={150}
            >
              <TaskRunButton {...props} refetch={refetch} />
              {!picked && taskInstance.length === 1 && (
                <DotButton
                  taskName={taskName}
                  taskInstance={taskInstance[0]}
                  style={{ marginRight: 5 }}
                />
              )}
            </Box>
            {taskInstance.length === 1 && <AccordionIcon />}
          </HStack>
        </HStack>
      </AccordionButton>
    </h2>
  );
};
