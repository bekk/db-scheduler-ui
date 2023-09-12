import React, { useRef, useState, useEffect } from 'react';
import { Task } from '../models/Task';
import TaskCard from './TaskCard';
import { Box } from '@chakra-ui/react';

interface TaskCardProps extends Task {
  refetch: () => void;
}

const TaskGroupCard: React.FC<TaskCardProps> = (taskProps) => {
  const [marginBottom, setMarginBottom] = useState(0);
  const ref = useRef<HTMLDivElement>(null);

  const updateRef = () => {
    if (ref.current) {
      const el = ref.current;
      setMarginBottom(-(el.clientHeight * 1.8));
    }
  };

  useEffect(() => {
    updateRef();
    window.addEventListener('resize', updateRef); // updateRef on resize to avoid overlapping
    return () => {
      window.removeEventListener('resize', updateRef);
    };
  }, []);

  return (
    <Box mb={`${marginBottom}px`}>
      <Box zIndex={0} mr={2} pos={'relative'}>
        <TaskCard
          {...taskProps}
          accordionProps={{
            shadow: 'md',
          }}
        />
      </Box>
      <Box
        pos={'relative'}
        left="0"
        zIndex={-3}
        mx={1}
        transform={'translateY(-90%)'}
      >
        <TaskCard
          {...taskProps}
          accordionProps={{
            shadow: 'md',
            mt: -1,
          }}
        />
      </Box>
      <Box
        ref={ref}
        pos={'relative'}
        left="0"
        zIndex={-4}
        ml={2}
        transform={'translateY(-180%)'}
      >
        <TaskCard
          {...taskProps}
          accordionProps={{
            shadow: 'md',
            mt: -1,
          }}
        />
      </Box>
    </Box>
  );
};

export default TaskGroupCard;
