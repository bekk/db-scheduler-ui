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
import { Badge, Tab, TabList, Tabs } from '@chakra-ui/react';
import {
  EXECUTION_STATUS_FILTERS,
  ExecutionStatusFilter,
} from 'src/models/ExecutionPage';
import { OverviewTask } from 'src/models/OverviewTask';
import colors from 'src/styles/colors';

const labels: Record<ExecutionStatusFilter, string> = {
  all: 'All',
  failing: 'Failing',
  running: 'Running',
  overdue: 'Overdue',
  scheduled: 'Scheduled',
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

export const StatusTabs: React.FC<{
  task: OverviewTask;
  status: ExecutionStatusFilter;
  onChange: (status: ExecutionStatusFilter) => void;
}> = ({ task, status, onChange }) => (
  <Tabs
    index={EXECUTION_STATUS_FILTERS.indexOf(status)}
    onChange={(index) => onChange(EXECUTION_STATUS_FILTERS[index])}
    size="sm"
    colorScheme="gray"
  >
    <TabList>
      {EXECUTION_STATUS_FILTERS.map((value) => {
        const n = count(task, value);
        return (
          <Tab key={value} color={colors.primary['500']}>
            {labels[value]}
            {n !== null && (
              <Badge
                ml={2}
                borderRadius="full"
                px={2}
                bg={
                  value === 'failing' && n > 0
                    ? colors.failed['100']
                    : colors.primary['200']
                }
                color={
                  value === 'failing' && n > 0
                    ? colors.failed['200']
                    : colors.primary['500']
                }
              >
                {n.toLocaleString()}
              </Badge>
            )}
          </Tab>
        );
      })}
    </TabList>
  </Tabs>
);
