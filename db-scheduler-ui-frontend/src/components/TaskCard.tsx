import React from 'react';

import { AccordionItem, Divider } from '@chakra-ui/react';
import { Task } from '../models/Task';

import { TaskAccordionButton } from 'src/components/TaskAccordionButton';
import { TaskAccordionItem } from 'src/components/TaskAccordionItem';

interface TaskCardProps extends Task {
  refetch: () => void;
}

const TaskCard: React.FC<TaskCardProps> = ({
  taskName,
  executionTime,
  taskData,
  consecutiveFailures,
  picked,
  taskInstance,
  lastFailure,
  lastSuccess,
  refetch,
}) => {
  return (
    <AccordionItem
      backgroundColor={'#FFFFFF'}
      borderRadius={4}
      m={1}
      borderWidth={1}
      borderColor={'#E0E0E0'}
    >
      <TaskAccordionButton
        picked={picked}
        taskInstance={taskInstance}
        taskName={taskName}
        consecutiveFailures={consecutiveFailures}
        executionTime={executionTime}
        taskData={taskData}
        refetch={refetch}
      />
      <Divider color={'#E0E0E0'} />
      <TaskAccordionItem
        lastSuccess={lastSuccess}
        lastFailure={lastFailure}
        taskData={taskData}
      />
    </AccordionItem>
  );
};
export default TaskCard;
