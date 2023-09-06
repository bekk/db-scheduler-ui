import React, { useState } from 'react';
import { Task } from '../models/Task';
import TaskCard from './TaskCard';
import { Box } from '@chakra-ui/react';

interface TaskCardProps extends Task {
  refetch: () => void;
}

const closedCardAttributes = {
  middleTransform: 'translateY(-60px)',
  bottomTransform: 'translateY(-120px)',
};
const openCardAttributes = {
  middleTransform: 'translateY(0px)',
  bottomTransform: 'translateY(0px)',
};

const TaskGroupCard: React.FC<TaskCardProps> = (taskProps) => {
  const [open, setOpen] = useState<boolean>(false);

  return (
    <Box position="relative" onClick={() => setOpen(!open)}>
      {/* Add some shadow later, must be within cards. Also consider removing margin between, as well as borders between */}
      <Box zIndex={2} mr={2} pos={'relative'}>
        <TaskCard
          {...taskProps}
          taskName="Top"
          accordionProps={{
            shadow: open ? 'none' : 'md',
          }}
          isGrouped
        />
      </Box>
      <Box
        //top="20px"
        pos={'relative'}
        left="0"
        zIndex={1}
        mx={1}
        transform={
          open
            ? openCardAttributes.middleTransform
            : closedCardAttributes.middleTransform
        }
      >
        <TaskCard
          {...taskProps}
          taskName="Middle"
          accordionProps={{
            shadow: open ? 'none' : 'md',
            mt: -1,
            isFocusable: true,
          }}
          isGrouped
        />
      </Box>
      <Box
        //top="10px"
        pos={'relative'}
        left="0"
        zIndex={0}
        ml={2}
        transform={
          open
            ? openCardAttributes.bottomTransform
            : closedCardAttributes.bottomTransform
        }
        mb={open ? '10px' : '-120px'}
      >
        <TaskCard
          {...taskProps}
          taskName="Bottom"
          accordionProps={{
            shadow: open ? 'none' : 'md',
            mt: -1,
            isFocusable: true,
          }} // This should probably be moved outside, to TaskList or something
          isGrouped
        />
      </Box>
    </Box>
  );
};

export default TaskGroupCard;
