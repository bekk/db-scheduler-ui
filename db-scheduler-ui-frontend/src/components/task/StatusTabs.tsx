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
import { Box, Text, Wrap, WrapItem } from '@chakra-ui/react';
import {
  EXECUTION_STATUS_FILTERS,
  ExecutionStatusFilter,
} from 'src/models/ExecutionPage';
import { OverviewTask } from 'src/models/OverviewTask';
import colors from 'src/styles/colors';
import { overdueColor } from 'src/utils/overdue';

const labels: Record<ExecutionStatusFilter, string> = {
  all: 'All',
  failing: 'Failing',
  running: 'Running',
  overdue: 'Overdue',
  scheduled: 'Scheduled',
};

const tones: Record<ExecutionStatusFilter, string> = {
  all: colors.primary['600'],
  failing: colors.failed['200'],
  running: colors.running['300'],
  overdue: overdueColor,
  scheduled: colors.primary['600'],
};

// Overdue has no count: the task summary does not carry one.
function count(
  task: OverviewTask,
  status: ExecutionStatusFilter,
): number | null {
  switch (status) {
    case 'all':
      return task.instanceCount;
    case 'failing':
      return task.counts.failing;
    case 'running':
      return task.counts.running;
    case 'scheduled':
      return task.counts.scheduled;
    default:
      return null;
  }
}

/**
 * The task page's tabs, drawn as the Overview's stat cards so the two pages read as one.
 * Unlike the Overview's cards these are single-select: exactly one is always chosen.
 */
export const StatusTabs: React.FC<{
  task: OverviewTask;
  status: ExecutionStatusFilter;
  onChange: (status: ExecutionStatusFilter) => void;
}> = ({ task, status, onChange }) => (
  <Wrap spacing={3} role="group" aria-label="Instances by status">
    {EXECUTION_STATUS_FILTERS.map((value) => {
      const n = count(task, value);
      const active = value === status;
      const tone = tones[value];
      return (
        <WrapItem key={value}>
          <Box
            as="button"
            type="button"
            aria-pressed={active}
            onClick={() => onChange(value)}
            minW="8.5rem"
            textAlign="left"
            bgColor={
              active && value === 'failing' ? '#fdf6f6' : colors.primary['100']
            }
            border="1px solid"
            borderColor={active ? tone : colors.primary['300']}
            boxShadow={active ? `inset 0 0 0 1px ${tone}` : undefined}
            borderRadius="8px"
            px={4}
            py={3}
            cursor="pointer"
            transition="border-color 0.12s ease"
            _hover={{ borderColor: tone }}
          >
            <Text
              fontSize="2xl"
              fontWeight="bold"
              lineHeight="1.2"
              // Zero is calm, not alarming, as on the Overview.
              color={
                n === null
                  ? colors.primary['300']
                  : n > 0
                  ? tone
                  : colors.primary['300']
              }
            >
              {n === null ? '–' : n.toLocaleString()}
            </Text>
            <Text
              fontSize="xs"
              fontWeight="bold"
              textTransform="uppercase"
              letterSpacing="wider"
              color={active ? tone : colors.primary['400']}
            >
              {labels[value]}
            </Text>
          </Box>
        </WrapItem>
      );
    })}
  </Wrap>
);
