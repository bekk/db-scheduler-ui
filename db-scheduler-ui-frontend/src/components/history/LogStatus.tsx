import { Box } from '@chakra-ui/react';
import colors from 'src/styles/colors';
interface LogStatusProps {
  succeeded: boolean;
}

export const LogStatus: React.FC<LogStatusProps> = ({ succeeded }) => {
  const borderColor = succeeded ? colors.success[200] : colors.failed[200];
  const backgroundColor = succeeded ? colors.success[100] : colors.failed[100];
  const color = succeeded ? colors.success[200] : colors.failed[200];

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
