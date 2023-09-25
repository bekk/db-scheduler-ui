import { Box } from '@chakra-ui/react';
import React from 'react';

import JsonViewer from 'src/components/JsonViewer';

export const LogDataRow: React.FC<{ taskData: object | null }> = ({
  taskData,
}) => {
  return (
    <>
      {taskData !== null && (
        <Box display={'flex'} flexDirection={'column'}>
          <JsonViewer data={taskData} />
        </Box>
      )}
    </>
  );
};
