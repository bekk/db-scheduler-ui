import React from 'react';

import { AccordionItem, AccordionItemProps, Divider } from '@chakra-ui/react';
import { Task } from '../models/Task';

import { TaskAccordionButton } from 'src/components/TaskAccordionButton';
import { TaskAccordionItem } from 'src/components/TaskAccordionItem';

interface TaskCardProps extends Task {
  refetch: () => void;
  accordionProps?: AccordionItemProps;
  isGrouped?: boolean;
}

const TaskCard: React.FC<TaskCardProps> = ({
  taskName,
  executionTime,
  actualTaskData,
  consecutiveFailures,
  picked,
  taskInstance,
  lastFailure,
  lastSuccess,
  refetch,
  accordionProps,
  isGrouped,
}) => {
  return (
    <AccordionItem
      backgroundColor={'#FFFFFF'}
      borderRadius={4}
      m={1}
      borderWidth={1}
      borderColor={'#E0E0E0'}
      {...accordionProps}
    >
      <TaskAccordionButton
        picked={picked}
        taskInstance={taskInstance}
        taskName={taskName}
        consecutiveFailures={consecutiveFailures}
        executionTime={executionTime}
        refetch={refetch}
      />
      {!isGrouped && (
        <>
          <Divider color={'#E0E0E0'} />
          <TaskAccordionItem
            lastSuccess={lastSuccess && lastSuccess[0]}
            lastFailure={lastFailure}
            taskData={actualTaskData}
          />
        </>
      )}
    </AccordionItem>
  );
};
export default TaskCard;
