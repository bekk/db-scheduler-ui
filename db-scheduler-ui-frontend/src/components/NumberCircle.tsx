import { Box, ResponsiveValue } from '@chakra-ui/react';
import { Property } from 'csstype';

interface NumberCircleProps {
  number: number;
  color?: string;
  position?: ResponsiveValue<Property.Position>;
  transform?: ResponsiveValue<Property.Transform>;
  style?: React.CSSProperties;
}

export const NumberCircle: React.FC<NumberCircleProps> = ({
  number,
  color,
  position = 'absolute',
  transform,
  style,
}) => {
  const powerOfTen = Math.floor(Math.log10(number));
  const isExpanded = 1 <= powerOfTen;

  const baseSize: number = 22;
  //const minWidth:number = 24;
  const maxWidth: number = 48;

  const width = isExpanded
    ? Math.min(maxWidth, baseSize + 7 * powerOfTen)
    : baseSize;

  const height: number = 22;

  const borderRadius = isExpanded
    ? `${baseSize / 2}px ${baseSize / 2}px ${baseSize / 2}px ${baseSize / 2}px`
    : '50%';
  const leftOffset = isExpanded ? (width - baseSize) / 2 : 0;

  return (
    <Box
      position={position}
      borderRadius={borderRadius}
      top={0}
      right={`${leftOffset}px`}
      width={`${width}px`}
      height={`${height}px`}
      backgroundColor={color ?? '#DAE2F6'}
      display="flex"
      justifyContent="center"
      alignItems={'center'}
      color={'white'}
      transform={transform}
      fontSize={'sm'}
      style={style}
    >
      {number}
    </Box>
  );
};
