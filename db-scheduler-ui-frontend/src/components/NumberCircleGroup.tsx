import React from 'react';
import { Task } from 'src/models/Task';
import { NumberCircle } from './NumberCircle';
import { Box } from '@chakra-ui/react';

export const NumberCircleGroup: React.FC<Task> = ({
  pickedBy,
  consecutiveFailures,
  taskInstance,
}) => {
  const runningCount = pickedBy?.reduce(
    (acc, entry) => (entry !== null ? acc + 1 : acc),
    0,
  );
  const failureCount = consecutiveFailures?.reduce(
    (acc, entry) => (entry > 0 ? acc + 1 : acc),
    0,
  );
  const scheduledCount = taskInstance.length - runningCount - failureCount;

  return (
    <Box display={'flex'}>
      {failureCount > 0 && (
        <NumberCircle
          number={failureCount}
          bgColor="#BB0101"
          position={'relative'}
        />
      )}
      {runningCount > 0 && (
        <NumberCircle
          number={runningCount}
          bgColor="#5068F6"
          position={'relative'}
        />
      )}
      {scheduledCount > 0 && (
        <NumberCircle
          number={scheduledCount}
          bgColor="#F1F2F5"
          textColor="#000000"
          position={'relative'}
        />
      )}
    </Box>
  );
};
