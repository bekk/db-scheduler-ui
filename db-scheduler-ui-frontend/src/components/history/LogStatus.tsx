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
      textAlign={'center'}
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
