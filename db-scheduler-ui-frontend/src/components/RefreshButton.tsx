import { Button } from '@chakra-ui/react';
import { RepeatIcon } from '@chakra-ui/icons';
import React from 'react';
import { Task } from 'src/models/Task';
import colors from 'src/styles/colors';

interface RefreshButtonProps extends Task {
  s: string;
}

export const RefreshButton: React.FC<RefreshButtonProps> = () => {
  return (
    <Button
      onClick={(event) => {
        console.log('event', event);
      }}
      iconSpacing={2}
      width={100}
      bgColor={colors.primary['100']}
      fontWeight="normal"
      rightIcon={<RepeatIcon />}
    >
      Refresh
    </Button>
  );
};
