import { Box, Text } from '@chakra-ui/react';
import React from 'react';
import colors from 'src/styles/colors';

export const LogDataRow: React.FC<{ taskData: object | null }> = ({
  taskData,
}) => {
  return (
    <>
      {taskData !== null && (
        <Box display={'flex'} flexDirection={'row'}>
          {Object.entries(taskData).map(([key, value]) => (
            <Box key={key} mr={4} display={'flex'} flexDirection={'column'}>
              <Text color={colors.primary['400']}>{key}</Text>{' '}
              <Text>{JSON.stringify(value)}</Text>
            </Box>
          ))}
        </Box>
      )}
    </>
  );
};
