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
      <Box
        borderRadius={'50%'}
        backgroundColor={'#E9ECFE'}
        width={'32px'}
        height={'32px'}
        justifyContent={'center'}
        alignItems={'center'}
        display={'flex'}
        mr={4}
      >
        <Text color={'#002FA7'} fontSize={'sm'}>
          ET
        </Text>
      </Box>
    </Box>
  );
};
