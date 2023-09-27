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
import { NumberCircle } from 'src/components/common/NumberCircle';
import colors from 'src/styles/colors';

interface StatusBoxProps {
  status: string;
  consecutiveFailures: number;
}

const statusColors: Record<
  string,
  { borderColor: string; backgroundColor: string; color: string }
> = {
  Failed: {
    borderColor: colors.failed['200'],
    backgroundColor: colors.failed['100'],
    color: colors.failed['200'],
  },
  Running: {
    borderColor: colors.running['100'],
    backgroundColor: colors.running['100'],
    color: colors.primary['900'],
  },
  Scheduled: {
    borderColor: colors.primary['300'],
    backgroundColor: colors.primary['200'],
    color: colors.primary['900'],
  },
  Group: {
    borderColor: colors.primary['900'],
    backgroundColor: colors.primary['200'],
    color: colors.primary['900'],
  },
};

export const StatusBox: React.FC<StatusBoxProps> = ({
  status,
  consecutiveFailures,
}) => {
  const statusInfo =
    status === 'Group'
      ? statusColors['Group']
      : statusColors[status] || statusColors['Scheduled'];
  const { borderColor, backgroundColor, color } = statusInfo;

  return (
    <Box
      textAlign={'center'}
      borderRadius={4}
      mr={4}
      width={115}
      borderColor={borderColor}
      backgroundColor={backgroundColor}
      color={color}
      px={4}
      py={1}
      borderWidth={1}
      position="relative"
      borderStyle={status !== 'Group' ? 'solid' : 'dashed'}
    >
      {consecutiveFailures > 0 && status !== 'Group' ? (
        <NumberCircle
          number={consecutiveFailures}
          bgColor={colors.failed['200']}
          transform={'translate(50%, -50%)'}
        ></NumberCircle>
      ) : (
        <></>
      )}

      {status}
    </Box>
  );
};
