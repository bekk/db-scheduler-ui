import { Box } from '@chakra-ui/react';
import { useState } from 'react';
import { NumberCircle } from './NumberCircle';

type RefreshCircleProps = {
  number: number;
  color: string;
  textColor?: string;
  visible?: boolean;
  hoverText: string;
};

export const RefreshCircle: React.FC<RefreshCircleProps> = ({
  number,
  color,
  textColor,
  visible,
  hoverText,
}) => {
  const [hovered, setHovered] = useState(false);

  const text = hovered ? hoverText : '';

  const powerOfTen = (number + hoverText).length - 1;
  const isExpanded = 1 <= powerOfTen;
  const baseSize: number = 22;
  const width = isExpanded ? baseSize + 7 * powerOfTen : baseSize;

  const marginLeft = isExpanded ? -width : 0;

  return (
    <Box
      alignItems={'end'}
      display={'flex'}
      justifyContent={'flex-end'}
      overflow={'visible'}
      ml={marginLeft}
      position="relative"
      visibility={visible ? 'visible' : 'hidden'}
    >
      <Box
        onMouseEnter={() => setHovered(true)}
        onMouseLeave={() => setHovered(false)}
      >
        <NumberCircle
          number={number + text}
          bgColor={color}
          textColor={textColor}
          position="relative"
          top={'auto'}
          style={{ bottom: '0', left: '0' }}
        />
      </Box>
    </Box>
  );
};
