import { Box } from '@chakra-ui/react';
import React, { useState } from 'react';
import { NumberCircle } from './NumberCircle';
import { RefreshCircleProps } from './RefreshButton';

export const RefreshCircle: React.FC<RefreshCircleProps> = ({
  number,
  color,
}) => {
  const [hovered, setHovered] = useState(false);

  return (
    <Box
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      alignItems={'end'}
      display={'flex'}
      justifyContent={'flex-end'}
      overflow={'visible'}
      ml={'-0.75em'}
      position="relative"
    >
      <NumberCircle
        number={number}
        bgColor={color}
        position="relative"
        top={'auto'}
        style={{ bottom: '0', left: '0' }}
      />
      {hovered && (
        <Box position="absolute" right="100%" mr={2} whiteSpace="nowrap">
          Some Text
        </Box>
      )}
    </Box>
  );
};
