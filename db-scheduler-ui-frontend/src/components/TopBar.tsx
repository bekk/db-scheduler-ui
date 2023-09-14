import { Box, Text } from '@chakra-ui/react';
import React from 'react';
import { LogoIcon } from 'src/assets/icons/Logo';
import colors from 'src/styles/colors';

interface TopBarProps {
  title: string;
}

export const TopBar: React.FC<TopBarProps> = ({ title }) => {
  return (
    <Box
      backgroundColor={colors.primary['100']}
      display={'flex'}
      justifyContent={'space-between'}
      alignItems={'center'}
    >
      <Text
        color={colors.dbBlue}
        fontSize={'2xl'}
        p={4}
        fontWeight={'semibold'}
      >
        <LogoIcon mr={2} />
        {title}
      </Text>
    </Box>
  );
};
