import React from 'react';

import { AccordionItem, AccordionItemProps, Divider } from '@chakra-ui/react';
import { Task } from '../models/Task';

import { TaskAccordionButton } from 'src/components/TaskAccordionButton';
import { TaskAccordionItem } from 'src/components/TaskAccordionItem';
import { isStatus } from 'src/utils/determineStatus';

interface TaskCardProps extends Task {
  refetch: () => void;
  accordionProps?: AccordionItemProps;
}

const TaskCard: React.FC<TaskCardProps> = (props) => {
  const { accordionProps, lastSuccess, lastFailure, taskData } = props;

  return (
    <AccordionItem
      backgroundColor={'#FFFFFF'}
      borderRadius={4}
      m={1}
      borderWidth={1}
      borderColor={'#E0E0E0'}
      {...accordionProps}
      pos={'relative'}
    >
      <TaskAccordionButton {...props} />
      {!isStatus('Group', props) && (
        <>
          <Divider color={'#E0E0E0'} />
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
