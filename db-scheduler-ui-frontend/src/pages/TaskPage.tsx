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
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { format } from 'date-fns';
import {
  Link as RouterLink,
  useLocation,
  useNavigate,
  useParams,
} from 'react-router-dom';
import { InstanceDrawer } from 'src/components/overview/InstanceDrawer';
import { timeSections } from 'src/utils/timeSections';
import { GoToInstance } from 'src/components/task/GoToInstance';
import { RerunFailedButton } from 'src/components/task/RerunFailedButton';
import { TASK_EXECUTIONS_QUERY_KEY } from 'src/services/getTaskExecutions';
import { getShowHistory } from 'src/utils/config';
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
  const navigate = useNavigate();
  const queryClient = useQueryClient();
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
  // The order the table shows them in, which groups running and overdue rows first.
  const order = timeSections(rows, new Date()).flatMap((section) =>
    section.rows.map((row) => row.taskInstance),
  );
  const position = params.instance ? order.indexOf(params.instance) : -1;
  const neighbour = (offset: number) => {
    const id = order[position + offset];
    return id === undefined ? null : () => params.stepInstance(id);
  };

  const refresh = () => {
    void summary.refetch();
    void queryClient.invalidateQueries([TASK_EXECUTIONS_QUERY_KEY, taskName]);
  };

  const backTo: string =
    (location.state as { from?: string } | null)?.from ?? '/overview';

  return (
    // Capped so the row columns stay together on wide screens.
    <Box pt={6} pb={24} maxW="78rem" mx="auto">
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
        <Stack spacing={5}>
          <HStack justify="space-between" align="start">
            <Header task={task} />
            <FormControl display="flex" alignItems="center" width="auto" pt={2}>
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

          <StatusTabs task={task} status={status} onChange={params.setStatus} />

          <Flex wrap="wrap" gap={3} align="center">
            <GoToInstance onOpen={params.openInstance} />
            <StartFromControl from={params.from} onChange={params.setFrom} />
            <Box flex={1} />
            <RerunFailedButton
              taskName={taskName}
              failing={task.counts.failing}
              onDone={refresh}
            />
          </Flex>

          {executions.isError && (
            <Text color={colors.failed['200']}>Could not load instances</Text>
          )}
          {executions.isSuccess && rows.length === 0 && (
            <Text color={colors.primary['400']}>
              {emptyText[status]}
              {params.from && (
                <>
                  {' '}
                  from {format(new Date(params.from), 'yyyy.MM.dd HH:mm:ss')} on
                  ·{' '}
                  <Button
                    variant="link"
                    size="sm"
                    color={colors.running['300']}
                    onClick={() => params.setFrom(null)}
                  >
                    show from the earliest
                  </Button>
                </>
              )}
            </Text>
          )}
          {rows.length > 0 && (
            <ExecutionTable
              rows={rows}
              selected={params.instance}
              onSelect={params.openInstance}
              hideStatus={status !== 'all'}
              nextRunLabel={status === 'failing' ? 'Retry' : 'Next run'}
            />
          )}
          {rows.length > 0 && (
            <Flex justify="center" align="center" gap={3}>
              {executions.hasNextPage && (
                <Button
                  size="sm"
                  variant="outline"
                  bgColor="white"
                  onClick={() => void executions.fetchNextPage()}
                  isLoading={executions.isFetchingNextPage}
                >
                  Load 50 more
                </Button>
              )}
              <Text fontSize="sm" color={colors.primary['400']}>
                {shownText(
                  rows.length,
                  tabCount(task, status),
                  !!executions.hasNextPage,
                )}
              </Text>
            </Flex>
          )}
          {getShowHistory() && (
            <Button
              variant="link"
              size="sm"
              alignSelf="start"
              color={colors.running['300']}
              onClick={() => navigate('/history/all', { state: { taskName } })}
            >
              View history for this task →
            </Button>
          )}
        </Stack>
      )}
      <InstanceDrawer
        taskName={params.instance ? taskName : null}
        instanceId={params.instance}
        stepper={
          // Opened by id for an instance outside the loaded rows, there is nothing to step through.
          position < 0
            ? undefined
            : {
                position,
                loaded: order.length,
                hasMore: !!executions.hasNextPage,
                onPrevious: neighbour(-1),
                onNext: neighbour(1),
              }
        }
        onClose={params.closeInstance}
        onChanged={refresh}
      />
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
    {/* Counts live on the status cards below; this line is about timing. */}
    <HStack spacing={2} fontSize="sm" color={colors.primary['400']} mt={1}>
      {task.recurring !== null && (
        <>
          <Text>{task.recurring ? 'Recurring' : 'One-time'}</Text>
          <Text>·</Text>
        </>
      )}
      <Text title={absoluteTitle(task.nextExecutionTime)}>
        next run{' '}
        <Text as="span" fontWeight="semibold" color={colors.primary['600']}>
          {nextRunText(task)}
        </Text>
      </Text>
      <Text>·</Text>
      {lastRunText(task)}
    </HStack>
  </Box>
);

function tabCount(
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

function shownText(shown: number, total: number | null, more: boolean): string {
  if (!more) {
    return shown === 1
      ? 'The only instance'
      : `All ${shown.toLocaleString()} shown`;
  }
  return total === null
    ? `Showing ${shown.toLocaleString()}`
    : `Showing ${shown.toLocaleString()} of ${total.toLocaleString()}`;
}
