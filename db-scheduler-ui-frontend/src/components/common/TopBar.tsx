/*
 * Copyright (C) Bekk
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { Box, Button, Text } from '@chakra-ui/react';
import React from 'react';
import { LogoIcon } from 'src/assets/icons/Logo';
import colors from 'src/styles/colors';
import { useNavigate } from 'react-router-dom';
import { getShowHistory } from 'src/utils/config';

interface TopBarProps {
  title: string;
}


export const TopBar: React.FC<TopBarProps> = ({ title }) => {
  const navigate = useNavigate();
  const showHistory= getShowHistory()

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
        {showHistory && (
          <>
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
              borderColor={
                !window.location.toString().includes('db-scheduler/history/')
                  ? colors.dbBlue
                  : colors.primary['300']
              }
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
              borderColor={
                window.location.toString().includes('history')
                  ? colors.dbBlue
                  : colors.primary['300']
              }
              onClick={() => navigate(`/history/all`)}
              aria-label={'History button'}
            >
              History
            </Button>
          </>
        )}
      </Box>
    </Box>
  );
};
