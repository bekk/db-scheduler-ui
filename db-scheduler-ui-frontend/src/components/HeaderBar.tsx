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
import { Box, Button, Input, Text } from '@chakra-ui/react';
import { FilterBy } from 'src/models/QueryParams';
import { FilterBox } from './FilterBox';
import { RefreshButton } from 'src/components/RefreshButton';
import { QueryObserverResult, InfiniteData } from '@tanstack/react-query';
import { InfiniteScrollResponse } from 'src/models/TasksResponse';
import { Log } from 'src/models/Log';
import { Task } from 'src/models/Task';
import { POLL_LOGS_QUERY_KEY, pollLogs } from 'src/services/pollLogs';
import { POLL_TASKS_QUERY_KEY, pollTasks } from 'src/services/pollTasks';
import { PlayIcon, RepeatIcon } from 'src/assets/icons';
import colors from 'src/styles/colors';
import runTaskGroup from 'src/services/runTaskGroup';

interface HeaderBarProps {
  inputPlaceholder: string;
  taskName: string;
  currentFilter: FilterBy;
  setCurrentFilter: (filter: FilterBy) => void;
  setSearchTerm: (searchTerm: string) => void;
  refetch?: () => Promise<
    QueryObserverResult<InfiniteData<InfiniteScrollResponse<Task | Log>>>
  >;
  title: string;
  history?: boolean;
}

export const HeaderBar: React.FC<HeaderBarProps> = ({
  inputPlaceholder,
  currentFilter,
  setCurrentFilter,
  setSearchTerm,
  refetch,
  title,
  history,
  taskName,
}) => (
  <Box
    display={'flex'}
    mb={7}
    alignItems={'center'}
    justifyContent={'space-between'}
    w={'100%'}
  >
    <Box display={'flex'} alignItems={'center'} flex={1}>
      <Box>
        <Box display={'flex'} alignItems={'center'}>
          <Text ml={1} fontSize={'3xl'} fontWeight={'semibold'}>
            {title}
          </Text>
          {taskName && (
            <>
              <Button
                rightIcon={<PlayIcon />}
                bgColor={colors.running['300']}
                textColor={colors.primary['100']}
                ml={5}
                onClick={() => {
                  runTaskGroup(taskName, false);
                }}
              >
                Run all
              </Button>
              <Button
                rightIcon={<RepeatIcon boxSize={6} />}
                bgColor={colors.failed['200']}
                textColor={colors.primary['100']}
                mx={5}
                onClick={() => {
                  runTaskGroup(taskName, true);
                }}
              >
                Rerun all failed
              </Button>
            </>
          )}
        </Box>
        <Input
          placeholder={inputPlaceholder}
          onChange={(e) => setSearchTerm(e.currentTarget.value)}
          bgColor={colors.primary['100']}
          w={'30vmax'}
          mt={7}
          ml={1}
        />
      </Box>
    </Box>
    <Box height={'100%'}>
      <FilterBox
        currentFilter={currentFilter}
        setCurrentFilter={setCurrentFilter}
        history={history}
      />
      <Box display={'flex'} float={'right'} alignItems={'center'}>
        <RefreshButton
          pollFunction={history ? pollLogs : pollTasks}
          pollKey={history ? POLL_LOGS_QUERY_KEY : POLL_TASKS_QUERY_KEY}
          refetch={refetch}
          params={{ filter: FilterBy.All }}
        />
      </Box>
    </Box>
  </Box>
);
