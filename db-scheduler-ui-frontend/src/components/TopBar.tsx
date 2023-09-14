import { Box, Text } from '@chakra-ui/react';
import React from 'react';
import { LogoIcon } from 'src/assets/icons/Logo';
import colors from 'src/styles/colors';
import { useNavigate } from 'react-router-dom';

interface TopBarProps {
  title: string;
}

export const TopBar: React.FC<TopBarProps> = ({ title }) => {
  const navigate = useNavigate();
  return (
    <Box
      backgroundColor={colors.primary['100']}
      display={'flex'}
      alignItems={'center'}
      gap={'12'}
    >
      <Text
        as={'button'}
        onClick={() => navigate('/')}
        aria-label={'Logo home button'}
        color={colors.dbBlue}
        fontSize={'2xl'}
        p={4}
        fontWeight={'semibold'}
      >
        <LogoIcon mr={2} />
        {title}
      </Text>
      <Box>
        <Text
          as={'button'}
          onClick={() => navigate('/')}
          aria-label={'Home button'}
          marginRight={12}
        >
          Scheduled
        </Text>
        <Text
          as={'button'}
          onClick={() => navigate(`/all`)}
          aria-label={'History button'}
        >
          History
        </Text>
      </Box>
    </Box>
  );
};
