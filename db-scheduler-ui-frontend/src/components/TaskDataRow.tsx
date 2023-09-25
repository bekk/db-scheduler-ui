import { Box } from '@chakra-ui/react';
import React from 'react';
import JsonViewer from 'src/components/JsonViewer';

export const TaskDataRow: React.FC<{ taskData: (object | null)[] }> = ({
  taskData,
}) => {
  return (
    <>
      {taskData[0] !== null && (
        <Box display={'flex'} flexDirection={'row'}>
          <JsonViewer data={taskData}></JsonViewer>
        </Box>
      )}
    </>
  );
};
