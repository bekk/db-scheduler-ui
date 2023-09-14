import { Box, Button, Text } from '@chakra-ui/react';
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
        <Button
          _hover={{
            bgColor: colors.primary['100'],
            borderColor: colors.dbBlue,
            color: colors.primary['400'],
          }}
          _active={{
            borderColor: colors.primary['200'],
            color: colors.primary['300'],
          }}
          bgColor={colors.primary['100']}
          color={colors.dbBlue}
          borderBottom="2px"
          borderRadius={'0'}
          borderColor={colors.primary['300']}
          onClick={() => navigate('/')}
          aria-label={'Home button'}
          marginRight={12}
        >
          Scheduled
        </Button>
        <Button
          _hover={{
            bgColor: colors.primary['100'],
            borderColor: colors.dbBlue,
            color: colors.primary['400'],
          }}
          _active={{
            borderColor: colors.running['200'],
            color: colors.primary['300'],
          }}
          bgColor={colors.primary['100']}
          color={colors.dbBlue}
          borderBottom="2px"
          borderRadius={'0'}
          borderColor={colors.primary['300']}
          onClick={() => navigate(`/history/all`)}
          aria-label={'History button'}
        >
          History
        </Button>
      </Box>
    </Box>
  );
};
