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
