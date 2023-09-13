import { Box, Text } from '@chakra-ui/react';

export const LogInfoBox: React.FC<{ taskName: string }> = ({ taskName }) => {
  return (
    <Box display={'flex'} mb={2} mt={2} alignItems={'center'}>
      <Text fontSize={'3xl'} fontWeight={'semibold'}>
        History for {taskName}
      </Text>
    </Box>
  );
};
