import { Box, Text } from '@chakra-ui/react';
import React from 'react';

export const TaskDataRow: React.FC<{ taskData: string | null }> = ({
  taskData,
}) => {
  return (
    <>
      {taskData && (
        <Box display={'flex'} flexDirection={'row'}>
          {Object.entries(taskData).map(([key, value]) => (
            <Box key={key} mr={4} display={'flex'} flexDirection={'column'}>
              <Text color={'#787878'}>{key}</Text>{' '}
              <Text>{JSON.stringify(value)}</Text>
            </Box>
          ))}
        </Box>
      )}
    </>
  );
};
