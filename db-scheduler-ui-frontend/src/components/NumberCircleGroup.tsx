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
import React from 'react';
import { Task } from 'src/models/Task';
import { NumberCircle } from './NumberCircle';
import { Box } from '@chakra-ui/react';
import colors from 'src/styles/colors';

export const NumberCircleGroup: React.FC<Task> = ({
  pickedBy,
  consecutiveFailures,
  taskInstance,
}) => {
  const runningCount = pickedBy?.reduce(
    (acc, entry) => (entry !== null ? acc + 1 : acc),
    0,
  );
  const failureCount = consecutiveFailures?.reduce(
    (acc, entry) => (entry > 0 ? acc + 1 : acc),
    0,
  );
  const scheduledCount = taskInstance.length - runningCount - failureCount;

  return (
    <Box display={'flex'}>
      {failureCount > 0 && (
        <NumberCircle
          number={failureCount}
          bgColor={colors.failed['200']}
          position={'static'}
          style={{ margin: '0 1px' }}
        />
      )}
      {runningCount > 0 && (
        <NumberCircle
          number={runningCount}
          bgColor={colors.running['300']}
          position={'static'}
          style={{ margin: '0 1px' }}
        />
      )}
      {scheduledCount > 0 && (
        <NumberCircle
          number={scheduledCount}
          bgColor={colors.primary['200']}
          textColor={colors.primary['900']}
          position={'static'}
          style={{ margin: '0 1px' }}
        />
      )}
    </Box>
  );
};
