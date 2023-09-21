import { Box, ResponsiveValue } from '@chakra-ui/react';
import { Property } from 'csstype';
import colors from 'src/styles/colors';

interface NumberCircleProps {
  number: number | string;
  bgColor?: string;
  textColor?: string;
  position?: ResponsiveValue<Property.Position>;
  transform?: ResponsiveValue<Property.Transform>;
  style?: React.CSSProperties;
  top?: string | number;
}

export const NumberCircle: React.FC<NumberCircleProps> = ({
  number,
  bgColor,
  textColor,
  position = 'absolute',
  transform,
  style,
  top,
}) => {
  const powerOfTen = Math.floor(Math.log10(number)); // TODO: Fix this
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
      top={top ?? 0}
      right={`${leftOffset}px`}
      width={`${width}px`}
      height={`${height}px`}
      backgroundColor={bgColor ?? colors.running['100']}
      display="flex"
      justifyContent="center"
      alignItems={'center'}
      color={textColor ?? colors.primary['100']}
      transform={transform}
      fontSize={'sm'}
      style={style}
    >
      {number}
    </Box>
  );
};
