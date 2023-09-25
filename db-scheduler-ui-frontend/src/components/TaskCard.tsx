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
import React from 'react';

import { AccordionItem, AccordionItemProps, Divider } from '@chakra-ui/react';
import { Task } from '../models/Task';

import { TaskAccordionButton } from 'src/components/TaskAccordionButton';
import { TaskAccordionItem } from 'src/components/TaskAccordionItem';
import { isStatus } from 'src/utils/determineStatus';
import colors from 'src/styles/colors';

interface TaskCardProps extends Task {
  refetch: () => void;
  accordionProps?: AccordionItemProps;
}

const TaskCard: React.FC<TaskCardProps> = (props) => {
  const { accordionProps, lastSuccess, lastFailure, taskData } = props;

  return (
    <AccordionItem
      backgroundColor={colors.primary['100']}
      borderRadius={4}
      m={1}
      borderWidth={1}
      borderColor={colors.primary['300']}
      {...accordionProps}
      pos={'relative'}
    >
      <TaskAccordionButton {...props} />
      {!isStatus('Group', props) && (
        <>
          <Divider color={colors.primary['300']} />
          <TaskAccordionItem
            lastSuccess={lastSuccess && lastSuccess[0]}
            lastFailure={lastFailure}
            taskData={taskData}
          />
        </>
      )}
    </AccordionItem>
  );
};
export default TaskCard;
