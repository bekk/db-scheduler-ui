import { Box } from '@chakra-ui/react';
interface LogStatusProps {
  succeeded: boolean;
}

export const LogStatus: React.FC<LogStatusProps> = ({ succeeded }) => {
  const borderColor = succeeded ? '#4CAF50' : '#BB0101';
  const backgroundColor = succeeded ? '#92c298' : '#EFC2C2';
  const color = succeeded ? '#4CAF50' : '#BB0101';

  return (
    <Box
      borderRadius={4}
      mr={4}
      width={120}
      borderColor={borderColor}
      backgroundColor={backgroundColor}
      color={color}
      px={4}
      py={1}
      borderWidth={1}
      position="relative"
    >
      {succeeded ? 'Succeeded' : 'Failed'}
    </Box>
  );
};
