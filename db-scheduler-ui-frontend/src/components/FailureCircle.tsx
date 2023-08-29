import { Box } from '@chakra-ui/react';

interface FailureCircleProps {
  consecutiveFailures: number;
}

export const FailureCircle: React.FC<FailureCircleProps> = ({
  consecutiveFailures,
}) => {
  const powerOfTen = Math.floor(Math.log10(consecutiveFailures));
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
      position="absolute"
      borderRadius={borderRadius}
      top={0}
      right={`${leftOffset}px`}
      width={`${width}px`}
      height={`${height}px`}
      backgroundColor="#BB0101"
      display="flex"
      justifyContent="center"
      alignItems={'center'}
      color={'white'}
      transform="translate(50%, -50%)"
      fontSize={'sm'}
    >
      {consecutiveFailures}
    </Box>
  );
};
