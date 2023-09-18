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
import { AttachmentIcon } from '@chakra-ui/icons';
import { determineStatus, isStatus } from 'src/utils/determineStatus';
import colors from 'src/styles/colors';

interface TaskAccordionButtonProps extends Task {
  refetch: () => void;
}

export const TaskAccordionButton: React.FC<TaskAccordionButtonProps> = (
  props,
) => {
  const {
    taskName,
    executionTime,
    consecutiveFailures,
    taskInstance,
    taskData,
    refetch,
  } = props;
  const { taskName: isDetailsView } = useParams<{ taskName?: string }>();
  return (
    <h2>
      <AccordionButton
        background={colors.primary['100']}
        _hover={{ backgroundColor: colors.primary['100'] }}
        cursor={!isStatus('Group', props) ? 'pointer' : 'default'}
      >
        <HStack w={'100%'} spacing={5}>
          <Box flex="1" display="inline-flex" alignItems={'center'}>
            <StatusBox
              status={determineStatus(props)}
              consecutiveFailures={
                consecutiveFailures.find((val) => val > 0) ?? 0
              }
            />
            {taskData[0] != null && (
              <AttachmentIcon
                position={'relative'}
                style={{ marginLeft: '-25', marginBottom: '-26' }}
              />
            )}
          </Box>
          {!isDetailsView && (
            <Box
              flex="2"
              textAlign="left"
              display={'flex'}
              alignItems={'center'}
            >
              {taskName}
              {isStatus('Group', props) && (
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
            {isStatus('Group', props) && (
              <Text color={colors.primary['500']}>
                {` + ${taskInstance.length - 1} more`}
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
                !isStatus('Group', props) ? 'space-between' : 'end'
              }
              w={150}
            >
              <TaskRunButton {...props} refetch={refetch} />
              <DotButton
                taskName={taskName}
                taskInstance={taskInstance[0]}
                style={{
                  marginRight: 5,
                  visibility:
                    !isStatus('Running', props) && !isStatus('Group', props)
                      ? 'visible'
                      : 'hidden',
                }}
              />
            </Box>
            <AccordionIcon
              visibility={!isStatus('Group', props) ? 'visible' : 'hidden'}
            />
          </HStack>
        </HStack>
      </AccordionButton>
    </h2>
  );
};
