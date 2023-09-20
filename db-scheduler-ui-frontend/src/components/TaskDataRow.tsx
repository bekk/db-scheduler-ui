import { Box, Text } from '@chakra-ui/react';
import React from 'react';
import colors from 'src/styles/colors';

export const TaskDataRow: React.FC<{ taskData: (object | null)[] }> = ({
  taskData,
}) => {
  return (
    <>
      {taskData[0] !== null && (
        <Box display={'flex'} flexDirection={'row'}>
          {Object.entries(taskData.length > 1 ? taskData : taskData[0]).map(
            ([key, value]) => (
              <Box key={key} mr={4} display={'flex'} flexDirection={'column'}>
                <Text color={colors.primary['400']}>{key}</Text>{' '}
                <Text>{JSON.stringify(value)}</Text>
              </Box>
            ),
          )}
        </Box>
      )}
    </>
  );
};
