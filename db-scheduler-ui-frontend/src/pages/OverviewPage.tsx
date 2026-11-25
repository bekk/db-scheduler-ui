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
  Button,
  FormControl,
  FormLabel,
  Heading,
  HStack,
  Stack,
  Switch,
  Table,
  TableContainer,
  Tbody,
  Td,
  Text,
  Tr,
} from '@chakra-ui/react';
import { ArrowForwardIcon } from '@chakra-ui/icons';
import { useQuery } from '@tanstack/react-query';
import { RepeatIcon } from 'src/assets/icons';
import { DetailsButton } from 'src/components/common/DetailsButton';
import { InstanceDrawer } from 'src/components/overview/InstanceDrawer';
import { SummaryStrip } from 'src/components/overview/SummaryStrip';
import { OverviewTask } from 'src/models/OverviewTask';
import {
  getOverviewTasks,
  OVERVIEW_TASKS_QUERY_KEY,
} from 'src/services/getOverviewTasks';
import colors from 'src/styles/colors';
import { useAutoRefresh } from 'src/hooks/useAutoRefresh';
import { useOverviewFilters } from 'src/hooks/useOverviewFilters';
import { useSelectedInstance } from 'src/hooks/useSelectedInstance';
import { overdueColor } from 'src/utils/overdue';
import { applyOverviewFilters } from 'src/utils/overviewFilters';
import { summarizeOverview } from 'src/utils/overviewSummary';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  absoluteTitle,
  dotColor,
  lastRunText,
  nextRunText,
  overdue,
  subLine,
} from 'src/utils/overviewWording';

// Matches the app-wide default in App.tsx; named here because this query overrides it.
const AUTO_REFRESH_MS = 2000;

export const OverviewPage: React.FC = () => {
  const { enabled: autoRefresh, setEnabled: setAutoRefresh } = useAutoRefresh();
  const {
    data = [],
    isLoading,
    isError,
    refetch,
  } = useQuery([OVERVIEW_TASKS_QUERY_KEY], getOverviewTasks, {
    refetchInterval: autoRefresh ? AUTO_REFRESH_MS : false,
  });
  const { activeFilters, toggleFilter, clearFilters } = useOverviewFilters();
  const { selected, select, clear } = useSelectedInstance();

  const tasks = sortedByName(data);
  const visibleTasks = applyOverviewFilters(tasks, activeFilters);
  const loaded = !isLoading && !isError;

  return (
    <Box>
      <Stack spacing={4} pt={10} pb={2}>
        <HStack justify="space-between" align="center">
          <Heading as="h1" size="lg" color={colors.primary['600']}>
            All tasks
          </Heading>
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
        {loaded && (
          <SummaryStrip
            summary={summarizeOverview(tasks)}
            activeFilters={activeFilters}
            shownCount={visibleTasks.length}
            onToggleFilter={toggleFilter}
            onClearFilters={clearFilters}
          />
        )}
      </Stack>
      <TableContainer overflowX="auto">
        <Table
          variant="simple"
          size="md"
          sx={{ borderCollapse: 'separate', borderSpacing: '0 8px' }}
        >
          <Tbody>
            {isLoading && <MessageRow message="Loading tasks" />}
            {isError && <MessageRow message="Could not load tasks" />}
            {loaded && (
              <>
                {visibleTasks.length > 0 && <ColumnLabels />}
                {visibleTasks.map((task) => (
                  <OverviewRow
                    key={task.taskName}
                    task={task}
                    onOpenInstance={select}
                  />
                ))}
                {tasks.length > 0 && visibleTasks.length === 0 && (
                  <MessageRow message="No tasks match the active filters" />
                )}
              </>
            )}
          </Tbody>
        </Table>
      </TableContainer>
      <InstanceDrawer
        taskName={selected}
        showTaskPageLink
        onClose={clear}
        onChanged={() => void refetch()}
      />
    </Box>
  );
};

const columnLabelSx = {
  textTransform: 'uppercase' as const,
  letterSpacing: 'wider',
  fontSize: 'xs',
  fontWeight: 'bold',
  color: colors.primary['400'],
  border: 'none',
  borderBottom: '1px solid',
  borderBottomColor: colors.primary['300'],
  pt: 1,
  pb: 2,
};

const ColumnLabels: React.FC = () => (
  <Tr>
    <Td sx={columnLabelSx}>Task</Td>
    <Td sx={columnLabelSx} width="18%">
      Next run
    </Td>
    <Td sx={columnLabelSx} width="18%">
      Last run
    </Td>
    <Td sx={columnLabelSx} width="1%" />
  </Tr>
);

const OverviewRow: React.FC<{
  task: OverviewTask;
  onOpenInstance: (taskName: string) => void;
}> = ({ task, onOpenInstance }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const drillDownTarget = `/tasks/${encodeURIComponent(task.taskName)}`;
  // Single-instance rows open the instance directly; the rest drill down to the list.
  const opensInstance = task.instanceCount === 1;

  const open = () => {
    if (opensInstance) {
      onOpenInstance(task.taskName);
    } else if (task.instanceCount > 0) {
      // Hands the task page the Overview's URL, filters included, for its breadcrumb.
      navigate(drillDownTarget, {
        state: { from: location.pathname + location.search },
      });
    }
  };

  // The row and its button say where a click leads, since that depends on the instance count.
  const hint = opensInstance
    ? 'Opens the instance in the side panel'
    : task.instanceCount > 1
    ? `Opens the task page · ${task.instanceCount.toLocaleString()} instances`
    : undefined;

  return (
    <Tr
      sx={rowSx(task, task.instanceCount > 0)}
      onClick={open}
      cursor={task.instanceCount > 0 ? 'pointer' : 'default'}
      title={hint}
    >
      <Td>
        <HStack align="center" spacing={2}>
          <Box
            aria-hidden="true"
            bgColor={dotColor(task.worstStatus)}
            borderRadius="50%"
            flexShrink={0}
            height="0.75rem"
            width="0.75rem"
            // No status dot for empty tasks; hidden (not removed) keeps names aligned.
            visibility={task.instanceCount === 0 ? 'hidden' : 'visible'}
          />
          <TaskTypeIcon task={task} />
          <Box minW={0}>
            <Text fontWeight="bold">{task.taskName}</Text>
            <Box color={colors.primary['400']} fontSize="sm">
              {subLine(task)}
            </Box>
          </Box>
        </HStack>
      </Td>
      <Td>
        <HStack spacing={2}>
          <ProximityDot task={task} />
          <Text
            title={absoluteTitle(task.nextExecutionTime)}
            color={overdue(task) ? overdueColor : colors.primary['500']}
            fontWeight={
              overdue(task) || task.counts.running > 0 ? 'semibold' : 'normal'
            }
          >
            {nextRunText(task)}
          </Text>
        </HStack>
      </Td>
      <Td>{lastRunText(task)}</Td>
      <Td textAlign="right" width="1%" whiteSpace="nowrap">
        {opensInstance && (
          <DetailsButton
            label={`Show details for ${task.taskName} in the side panel`}
            onOpen={open}
          />
        )}
        {task.instanceCount > 1 && (
          <Button
            size="xs"
            variant="outline"
            bgColor="white"
            color={colors.primary['600']}
            rightIcon={<ArrowForwardIcon />}
            aria-label={`Open the task page for ${task.taskName}`}
            onClick={(event) => {
              event.stopPropagation();
              open();
            }}
          >
            {task.instanceCount.toLocaleString()} instances
          </Button>
        )}
      </Td>
    </Tr>
  );
};

// Recurring tasks are marked with a repeat icon; everything else gets an empty slot of the
// same width so task names stay aligned. Tasks whose type the server could not determine
// (recurring === null) are left unmarked rather than implicitly labelled one-time.
const TaskTypeIcon: React.FC<{ task: OverviewTask }> = ({ task }) =>
  task.recurring === true ? (
    <Box
      as="span"
      role="img"
      aria-label="Recurring task"
      title="Recurring task"
      display="flex"
      alignItems="center"
      flexShrink={0}
      color={colors.primary['400']}
    >
      <RepeatIcon boxSize={5} />
    </Box>
  ) : (
    <Box aria-hidden="true" flexShrink={0} boxSize="1.25rem" />
  );

// A dot whose opacity grows as the next run approaches — full for imminent/running,
// faint for the far future. Rendered invisible (but space-preserving) when there is no
// upcoming run, so the next-run text stays aligned across rows.
const ProximityDot: React.FC<{ task: OverviewTask }> = ({ task }) => {
  const opacity = proximityOpacity(task);
  return (
    <Box
      aria-hidden="true"
      bgColor={colors.primary['600']}
      borderRadius="50%"
      flexShrink={0}
      height="0.5rem"
      width="0.5rem"
      opacity={opacity ?? 0}
    />
  );
};

const MessageRow: React.FC<{ message: string }> = ({ message }) => (
  <Tr>
    <Td colSpan={4}>
      <Text color={colors.primary['400']}>{message}</Text>
    </Td>
  </Tr>
);

// Stable alphabetical order, independent of run times — proximity is conveyed by the
// next-run dot, not by row position (see proximityOpacity).
function sortedByName(tasks: OverviewTask[]): OverviewTask[] {
  return [...tasks].sort((a, b) => a.taskName.localeCompare(b.taskName));
}

// 1 (imminent/overdue/running) → ~0.15 (far future); null when there is no upcoming run.
// Logarithmic over seconds so the dot fades legibly across the ranges that matter here:
// ~10s→1.0, 1min→0.8, 10min→0.56, 1h→0.36, ≥1d→0.15.
function proximityOpacity(task: OverviewTask): number | null {
  if (task.worstStatus === 'DORMANT' || !task.nextExecutionTime) {
    return null;
  }
  if (task.counts.running > 0) {
    return 1;
  }
  const secondsUntil =
    (new Date(task.nextExecutionTime).getTime() - Date.now()) / 1000;
  if (secondsUntil <= 10) {
    return 1;
  }
  return Math.min(1, Math.max(0.15, 1 - (Math.log10(secondsUntil) - 1) / 4));
}

function rowBackground(task: OverviewTask): string | undefined {
  if (task.worstStatus === 'FAILING') {
    return '#fcf3f3';
  }
  if (task.worstStatus === 'RUNNING') {
    return '#f6f8fd';
  }
  if (task.worstStatus === 'DORMANT') {
    return '#f8f9fa';
  }
  return colors.primary['100'];
}

function rowHoverBackground(task: OverviewTask): string {
  if (task.worstStatus === 'FAILING') {
    return '#f7e8e8';
  }
  if (task.worstStatus === 'RUNNING') {
    return '#eef2fc';
  }
  if (task.worstStatus === 'DORMANT') {
    return '#f1f2f3';
  }
  return colors.primary['200'];
}

function rowBorderColor(task: OverviewTask): string {
  if (task.worstStatus === 'FAILING') {
    return '#f3dede';
  }
  if (task.worstStatus === 'RUNNING') {
    return '#e7edf9';
  }
  if (task.worstStatus === 'DORMANT') {
    return '#eceef0';
  }
  return colors.primary['300'];
}

function rowSx(task: OverviewTask, clickable = false) {
  return {
    '& > td': {
      bgColor: rowBackground(task),
      borderBottom: '1px solid',
      borderBottomColor: rowBorderColor(task),
      borderTop: '1px solid',
      borderTopColor: rowBorderColor(task),
      py: 4,
      transition: 'background-color 0.12s ease',
    },
    '& > td:first-of-type': {
      borderLeft: '1px solid',
      borderLeftColor: rowBorderColor(task),
      borderBottomLeftRadius: '8px',
      borderTopLeftRadius: '8px',
    },
    '& > td:last-of-type': {
      borderRight: '1px solid',
      borderRightColor: rowBorderColor(task),
      borderBottomRightRadius: '8px',
      borderTopRightRadius: '8px',
    },
    ...(clickable && {
      transition: 'filter 0.12s ease',
      '&:hover > td': { bgColor: rowHoverBackground(task) },
      '&:hover': { filter: 'drop-shadow(0 2px 6px rgba(0,0,0,0.12))' },
    }),
  };
}
