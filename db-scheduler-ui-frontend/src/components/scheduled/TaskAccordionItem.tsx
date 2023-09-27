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
import { AccordionPanel, Box, VStack } from '@chakra-ui/react';
import { TaskDataRow } from 'src/components/scheduled/TaskDataRow';
import React from 'react';
import { dateFormatText } from 'src/utils/dateFormatText';
import colors from 'src/styles/colors';

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
  <AccordionPanel overflowX="auto" pb={4}>
    <Box display="flex" justifyContent={'space-between'}>
      <VStack
        align="start"
        spacing={2}
        bgColor={colors.primary['100']}
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
