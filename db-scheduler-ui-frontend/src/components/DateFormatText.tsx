import React from 'react';
import { Box } from '@chakra-ui/react';

interface DateFormatProps {
  executionDate: Date;
}
export const DateFormatText: React.FC<DateFormatProps> = ({
  executionDate,
}) => {
  return (
    <Box flex="1">
      {new Intl.DateTimeFormat('en-GB', {
        day: '2-digit',
      }).format(executionDate)}
      .{' '}
      {new Intl.DateTimeFormat('en-GB', {
        month: 'short',
      }).format(executionDate)}{' '}
      {new Intl.DateTimeFormat('en-GB', {
        year: '2-digit',
      }).format(executionDate)}
      , {executionDate?.toLocaleTimeString()}
    </Box>
  );
};
