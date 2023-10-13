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
import { Box, Button } from '@chakra-ui/react';
import { RepeatIcon } from '@chakra-ui/icons';
import React from 'react';
import colors from 'src/styles/colors';
import { InfiniteData, QueryObserverResult } from '@tanstack/react-query';
import { TaskDetailsRequestParams } from 'src/models/TaskRequestParams';
import { InfiniteScrollResponse } from 'src/models/TasksResponse';
import { RefreshCircle } from 'src/components/common/RefreshCircle';
import { Log } from 'src/models/Log';
import { Task } from 'src/models/Task';
import { PollResponse } from 'src/models/PollResponse';

interface RefreshButtonProps {
  refetch?: () => Promise<
    QueryObserverResult<InfiniteData<InfiniteScrollResponse<Task | Log>>>
  >;
  pollFunction: (params: TaskDetailsRequestParams) => Promise<PollResponse>;
  pollKey: string;
  params: TaskDetailsRequestParams;
  pollData: PollResponse | undefined;
}

export const RefreshButton: React.FC<RefreshButtonProps> = ({
  refetch,
  pollData,
}) => {
  return (
    <Box position="relative" display="inline-block">
      <Button
        onClick={() => {
          refetch && refetch();
        }}
        iconSpacing={3}
        p={4}
        py={7}
        borderColor={colors.primary['300']}
        borderWidth={1}
        bgColor={colors.primary['100']}
        fontWeight="normal"
        rightIcon={<RepeatIcon boxSize={'7'} />}
      >
        <Box textAlign={'left'}>Refresh</Box>
      </Button>
      <Box
        pos="absolute"
        left={2}
        justifyContent={'flex-end'}
        top={-1}
        display="flex"
        flexDirection="column"
      >
        <RefreshCircle
          number={pollData?.newFailures ?? 0}
          color={colors.failed['200']}
          visible={
            pollData?.newFailures !== 0 && pollData?.newFailures !== undefined
          }
          hoverText=" failed since refresh"
        />
        {pollData?.newSucceeded ? (
          <RefreshCircle
            number={pollData?.newSucceeded ?? 0}
            color={colors.success['200']}
            visible={
              pollData?.newSucceeded !== 0 &&
              pollData?.newSucceeded !== undefined
            }
            hoverText=" succeeded since refresh"
          />
        ) : (
          <RefreshCircle
            number={pollData?.newRunning ?? 0}
            color={colors.running['300']}
            visible={
              pollData?.newRunning !== 0 && pollData?.newRunning !== undefined
            }
            hoverText=" running since refresh"
          />
        )}

        <RefreshCircle
          number={pollData?.newTasks ?? 0}
          color={colors.primary['300']}
          textColor={colors.primary['900']}
          visible={pollData?.newTasks !== 0 && pollData?.newTasks !== undefined}
          hoverText=" added since refresh"
        />
      </Box>
    </Box>
  );
};
