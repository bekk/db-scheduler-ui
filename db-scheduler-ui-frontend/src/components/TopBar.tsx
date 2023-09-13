import { Box, Text } from '@chakra-ui/react';
import React from 'react';
import { LogoIcon } from 'src/assets/icons/Logo';

interface TopBarProps {
  title: string;
}

export const TopBar: React.FC<TopBarProps> = ({ title }) => {
  return (
    <Box
      backgroundColor={'#FFFFFF'}
      display={'flex'}
      justifyContent={'space-between'}
      alignItems={'center'}
    >
      <Text color={'#002FA7'} fontSize={'2xl'} p={4} fontWeight={'semibold'}>
        <LogoIcon mr={2} />
        {title}
      </Text>
    </Box>
  );
};
