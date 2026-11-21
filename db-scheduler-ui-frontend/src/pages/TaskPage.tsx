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
import {
  Box,
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  Button,
  Flex,
  FormControl,
  FormLabel,
  Heading,
  HStack,
  Stack,
  Switch,
  Text,
} from '@chakra-ui/react';
import { useQuery } from '@tanstack/react-query';
import { Link as RouterLink, useLocation, useParams } from 'react-router-dom';
import { ExecutionTable } from 'src/components/task/ExecutionTable';
import { StartFromControl } from 'src/components/task/StartFromControl';
import { StatusTabs } from 'src/components/task/StatusTabs';
import { useAutoRefresh } from 'src/hooks/useAutoRefresh';
import { useTaskExecutions } from 'src/hooks/useTaskExecutions';
import { useTaskPageParams } from 'src/hooks/useTaskPageParams';
import { ExecutionStatusFilter } from 'src/models/ExecutionPage';
import { OverviewTask } from 'src/models/OverviewTask';
import {
  getOverviewTasks,
  OVERVIEW_TASKS_QUERY_KEY,
} from 'src/services/getOverviewTasks';
import colors from 'src/styles/colors';
import {
  absoluteTitle,
  dotColor,
  lastRunText,
  nextRunText,
  subLine,
} from 'src/utils/overviewWording';

// Same interval as the Overview, so the two pages refresh alike.
const AUTO_REFRESH_MS = 2000;

const emptyText: Record<ExecutionStatusFilter, string> = {
  all: 'No instances',
  failing: 'No failing instances',
  running: 'No running instances',
  overdue: 'No overdue instances',
  scheduled: 'No scheduled instances',
};

/** One task's executions, by status. See `specs/task-detail/spec.md`. */
export const TaskPage: React.FC = () => {
  const { taskName = '' } = useParams<{ taskName: string }>();
  const location = useLocation();
  const { enabled: autoRefresh, setEnabled: setAutoRefresh } = useAutoRefresh();
  const params = useTaskPageParams();
  const refetchInterval = autoRefresh ? AUTO_REFRESH_MS : false;

  const summary = useQuery([OVERVIEW_TASKS_QUERY_KEY], getOverviewTasks, {
    refetchInterval,
  });
  const task = summary.data?.find((t) => t.taskName === taskName);

  // Failing first when there is something failing; the choice is not written to the URL.
  const status: ExecutionStatusFilter =
    params.status ?? (task && task.counts.failing > 0 ? 'failing' : 'all');

  const executions = useTaskExecutions(taskName, status, params.from, {
    // Waits for the summary so the default tab is settled before the first fetch.
    enabled: !!task,
    refetchInterval,
  });
  const rows = executions.data?.pages.flatMap((page) => page.items) ?? [];

  const backTo: string =
    (location.state as { from?: string } | null)?.from ?? '/overview';

  return (
    <Box pt={6} pb={24}>
      <Breadcrumb fontSize="sm" color={colors.primary['400']} mb={2}>
        <BreadcrumbItem>
          <BreadcrumbLink as={RouterLink} to={backTo}>
            Overview
          </BreadcrumbLink>
        </BreadcrumbItem>
        <BreadcrumbItem isCurrentPage>
          <Text>{taskName}</Text>
        </BreadcrumbItem>
      </Breadcrumb>

      {summary.isLoading && <Text color={colors.primary['400']}>Loading</Text>}
      {summary.isError && (
        <Text color={colors.failed['200']}>Could not load the task</Text>
      )}
      {summary.isSuccess && !task && (
        <Stack spacing={2} pt={4}>
          <Heading as="h1" size="lg" color={colors.primary['600']}>
            {taskName}
          </Heading>
          <Text color={colors.primary['400']}>
            No scheduled instances for this task.
          </Text>
          <Button as={RouterLink} to={backTo} size="sm" alignSelf="start">
            Back to the Overview
          </Button>
        </Stack>
      )}

      {task && (
        <Stack spacing={4}>
          <HStack justify="space-between" align="start">
            <Header task={task} />
            <FormControl display="flex" alignItems="center" width="auto">
              <FormLabel
                htmlFor="auto-refresh"
                fontSize="sm"
                color={colors.primary['500']}
                mb={0}
                mr={2}
              >
                Auto-refresh
              </FormLabel>
              <Switch
                id="auto-refresh"
                size="sm"
                isChecked={autoRefresh}
                onChange={(e) => setAutoRefresh(e.target.checked)}
              />
            </FormControl>
          </HStack>

          <Flex wrap="wrap" gap={3} justify="space-between" align="center">
            <StatusTabs
              task={task}
              status={status}
              onChange={params.setStatus}
            />
            <StartFromControl from={params.from} onChange={params.setFrom} />
          </Flex>

          {executions.isError && (
            <Text color={colors.failed['200']}>Could not load instances</Text>
          )}
          {executions.isSuccess && rows.length === 0 && (
            <Text color={colors.primary['400']}>{emptyText[status]}</Text>
          )}
          {rows.length > 0 && (
            <ExecutionTable
              rows={rows}
              selected={params.instance}
              onSelect={params.openInstance}
            />
          )}
          {rows.length > 0 && (
            <Flex justify="center">
              <Button
                size="sm"
                variant="outline"
                onClick={() => void executions.fetchNextPage()}
                isDisabled={
                  !executions.hasNextPage || executions.isFetchingNextPage
                }
              >
                {executions.isFetchingNextPage
                  ? 'Loading…'
                  : executions.hasNextPage
                  ? 'Load more'
                  : 'Nothing more to load'}
              </Button>
            </Flex>
          )}
        </Stack>
      )}
    </Box>
  );
};

const Header: React.FC<{ task: OverviewTask }> = ({ task }) => (
  <Box>
    <HStack spacing={2} align="center">
      <Box
        aria-hidden="true"
        bgColor={dotColor(task.worstStatus)}
        borderRadius="50%"
        boxSize="0.75rem"
        flexShrink={0}
      />
      <Heading as="h1" size="lg" color={colors.primary['600']}>
        {task.taskName}
      </Heading>
    </HStack>
    <Box color={colors.primary['400']} fontSize="sm" mt={1}>
      {subLine(task)}
    </Box>
    <HStack spacing={2} fontSize="sm" color={colors.primary['500']} mt={1}>
      <Text title={absoluteTitle(task.nextExecutionTime)}>
        Next run {nextRunText(task)}
      </Text>
      <Text>·</Text>
      {lastRunText(task)}
    </HStack>
  </Box>
);
