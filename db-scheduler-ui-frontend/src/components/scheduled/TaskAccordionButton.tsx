/*
 * Copyright (C) Bekk
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
import {
  AccordionButton,
  AccordionIcon,
  Box,
  Flex,
  HStack,
  Text,
} from '@chakra-ui/react';
import { StatusBox } from 'src/components/common/StatusBox';
import { TaskRunButton } from 'src/components/input/TaskRunButton';
import React from 'react';
import { DotButton } from 'src/components/input/DotButton';
import { dateFormatText } from 'src/utils/dateFormatText';
import { useNavigate, useParams } from 'react-router-dom';
import { Task } from 'src/models/Task';
import { NumberCircleGroup } from 'src/components/common/NumberCircleGroup';
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
  const navigate = useNavigate();
  return (
    <h2>
      <AccordionButton
        overflowX="auto"
        as={'div'}
        borderRadius={4}
        background={colors.primary['100']}
        _hover={{ backgroundColor: colors.primary['200'], cursor: 'pointer' }}
        cursor={'pointer'}
        onClick={(event) => {
          event.stopPropagation();
          isStatus('Group', props)
            ? navigate(`/${taskName}`, {
                state: { taskName: taskName },
              })
            : undefined;
        }}
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
              minWidth={28}
              display={'flex'}
              alignItems={'center'}
              overflow={'hidden'}
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
            <Text minWidth={28}>
              {taskInstance[0].length > 40
                ? taskInstance[0].slice(0, 40) + '...'
                : taskInstance[0]}
            </Text>
            {isStatus('Group', props) && (
              <Text minWidth={28} color={colors.primary['500']}>
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
            <Box flex={1} minWidth={28}>
              {dateFormatText(new Date(executionTime[0]))}
            </Box>
            <Box
              display={'flex'}
              minWidth={28}
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
