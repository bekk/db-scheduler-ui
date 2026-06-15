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
  HStack,
  Table,
  TableContainer,
  Tbody,
  Td,
  Text,
  Tr,
} from '@chakra-ui/react';
import { HamburgerIcon } from '@chakra-ui/icons';
import { useQuery } from '@tanstack/react-query';
import { RepeatIcon } from 'src/assets/icons';
import { OverviewTask, OverviewTaskStatus } from 'src/models/OverviewTask';
import {
  getOverviewTasks,
  OVERVIEW_TASKS_QUERY_KEY,
} from 'src/services/getOverviewTasks';
import colors from 'src/styles/colors';
import { dateFormatText } from 'src/utils/dateFormatText';
import {
  formatDistanceStrict,
  formatDistanceToNowStrict,
  isBefore,
} from 'date-fns';
import { useNavigate } from 'react-router-dom';

const statusText: Record<OverviewTaskStatus, string> = {
  FAILING: 'failing',
  RUNNING: 'running',
  SCHEDULED: 'scheduled',
  DORMANT: 'dormant',
};

const statusColors: Record<OverviewTaskStatus, string> = {
  FAILING: colors.failed['200'],
  RUNNING: colors.running['300'],
  SCHEDULED: colors.success['200'],
  DORMANT: colors.primary['400'],
};

const overdueColor = '#725200';

export const OverviewPage: React.FC = () => {
  const {
    data = [],
    isLoading,
    isError,
  } = useQuery([OVERVIEW_TASKS_QUERY_KEY], getOverviewTasks);

  const recurringTasks = sortedByName(
    data.filter((task) => task.recurring === true),
  );
  const customTasks = sortedByName(
    data.filter((task) => task.recurring !== true),
  );

  return (
    <Box>
      <TableContainer overflowX="auto">
        <Table
          variant="simple"
          size="md"
          sx={{ borderCollapse: 'separate', borderSpacing: '0 8px' }}
        >
          <Tbody>
            {isLoading && <MessageRow message="Loading tasks" />}
            {isError && <MessageRow message="Could not load tasks" />}
            {!isLoading && !isError && (
              <>
                <Section
                  title="Recurring"
                  icon={<RepeatIcon boxSize={6} />}
                  tasks={recurringTasks}
                />
                <Section
                  title="One-time / custom"
                  icon={<HamburgerIcon boxSize={5} />}
                  tasks={customTasks}
                />
              </>
            )}
          </Tbody>
        </Table>
      </TableContainer>
    </Box>
  );
};

const Section: React.FC<{
  title: string;
  icon: React.ReactNode;
  tasks: OverviewTask[];
}> = ({ title, icon, tasks }) => (
  <>
    <SectionHeader title={title} icon={icon} count={tasks.length} />
    {tasks.length > 0 && <ColumnLabels />}
    {tasks.map((task) => (
      <OverviewRow key={task.taskName} task={task} />
    ))}
  </>
);

const SectionHeader: React.FC<{
  title: string;
  icon: React.ReactNode;
  count: number;
}> = ({ title, icon, count }) => (
  <Tr>
    <Td colSpan={3} border="none" pt={10} pb={1}>
      <HStack spacing={3} align="center">
        <Box
          aria-hidden="true"
          display="flex"
          alignItems="center"
          color={colors.primary['400']}
        >
          {icon}
        </Box>
        <Text fontSize="2xl" fontWeight="semibold" color={colors.primary['600']}>
          {title}
        </Text>
        <Text fontSize="lg" color={colors.primary['400']} fontWeight="normal">
          {count}
        </Text>
      </HStack>
    </Td>
  </Tr>
);

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
  </Tr>
);

const OverviewRow: React.FC<{ task: OverviewTask }> = ({ task }) => {
  const navigate = useNavigate();
  const drillDownTarget = `/scheduled/${encodeURIComponent(task.taskName)}`;

  return (
    <Tr
      sx={rowSx(task, task.instanceCount > 0)}
      onClick={() => task.instanceCount > 0 && navigate(drillDownTarget)}
      cursor={task.instanceCount > 0 ? 'pointer' : 'default'}
    >
      <Td>
        <HStack align="center" spacing={3}>
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
          <ProximityPillar task={task} />
          <Text
            title={absoluteTitle(task.nextExecutionTime)}
            color={nextRunColor(task)}
            fontWeight={nextRunFontWeight(task)}
          >
            {nextRunText(task)}
          </Text>
        </HStack>
      </Td>
      <Td>{lastRunText(task)}</Td>
    </Tr>
  );
};

// A pillar whose height grows as the next run approaches — tall for imminent/running,
// short for the far future, over a faint full-height track that keeps the level readable
// even when short. Bottom-anchored like a level gauge; a vertical bar reads distinctly
// from the round status dot beside the task name. Rendered as an empty (space-preserving)
// slot when there is no upcoming run, so the next-run text stays aligned across rows.
const PILLAR_SLOT_HEIGHT = '0.9375rem'; // 15px

const ProximityPillar: React.FC<{ task: OverviewTask }> = ({ task }) => {
  const level = proximityLevel(task);
  if (level === null) {
    return (
      <Box aria-hidden="true" flexShrink={0} width="4px" height={PILLAR_SLOT_HEIGHT} />
    );
  }
  return (
    <Box
      aria-hidden="true"
      position="relative"
      flexShrink={0}
      width="4px"
      height={PILLAR_SLOT_HEIGHT}
    >
      <Box
        position="absolute"
        left="1px"
        top={0}
        bottom={0}
        width="2px"
        borderRadius="1px"
        bgColor={colors.primary['200']}
      />
      <Box
        position="absolute"
        left={0}
        bottom={0}
        width="4px"
        borderRadius="2px"
        height={`${(3 + level * 12).toFixed(1)}px`}
        bgColor={pillarColor(task)}
      />
    </Box>
  );
};

function pillarColor(task: OverviewTask): string {
  if (task.counts.running > 0) {
    return colors.running['300'];
  }
  if (isOverdue(task)) {
    return overdueColor;
  }
  return colors.primary['600'];
}

const MessageRow: React.FC<{ message: string }> = ({ message }) => (
  <Tr>
    <Td colSpan={3}>
      <Text color={colors.primary['400']}>{message}</Text>
    </Td>
  </Tr>
);

// Stable alphabetical order, independent of run times — proximity is conveyed by the
// next-run pillar and text shade, not by row position (see proximityLevel).
function sortedByName(tasks: OverviewTask[]): OverviewTask[] {
  return [...tasks].sort((a, b) => a.taskName.localeCompare(b.taskName));
}

// 1 (imminent/overdue/running) → ~0.15 (far future); null when there is no upcoming run.
// Logarithmic over seconds so the pillar/text grade legibly across the ranges that matter
// here: ~10s→1.0, 1min→0.8, 10min→0.56, 1h→0.36, ≥1d→0.15.
function proximityLevel(task: OverviewTask): number | null {
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

function subLine(task: OverviewTask) {
  if (task.instanceCount > 1 && task.recurring !== true) {
    return (
      <HStack spacing={1} flexWrap="wrap">
        <Text as="span">{instancesText(task.instanceCount)}</Text>
        {countPart(task.counts.failing, 'failing', colors.failed['200'])}
        {countPart(task.counts.running, 'running', colors.running['300'])}
        {countPart(task.counts.scheduled, 'scheduled', colors.primary['500'])}
      </HStack>
    );
  }

  const duration = statusDuration(task);
  const parts: string[] = [];

  if (task.worstStatus !== 'DORMANT') {
    const base = statusText[task.worstStatus];
    parts.push(duration ? `${base} for ${duration}` : base);
  }

  if (task.recurring !== true) {
    parts.push(instancesText(task.instanceCount));
  }

  return parts.join(' · ');
}

function statusDuration(task: OverviewTask): string | null {
  if (task.worstStatus === 'FAILING' && task.lastSuccess) {
    return formatDistanceStrict(new Date(task.lastSuccess), new Date());
  }

  if (
    task.worstStatus === 'RUNNING' &&
    task.instanceCount === 1 &&
    task.nextExecutionTime
  ) {
    return formatDistanceStrict(new Date(task.nextExecutionTime), new Date());
  }

  return null;
}

function nextRunText(task: OverviewTask): string {
  if (task.worstStatus === 'DORMANT' || !task.nextExecutionTime) {
    return '-';
  }

  if (task.counts.running > 0) {
    return 'running now';
  }

  const date = new Date(task.nextExecutionTime);
  const distance = formatDistanceToNowStrict(date);
  return isBefore(date, new Date()) ? `due ${distance} ago` : `in ${distance}`;
}

// Next-run text shade reinforces the pillar: light gray when the run is far off, deepening
// toward near-black as it approaches. Overdue keeps its amber; dormant rows stay muted.
function nextRunColor(task: OverviewTask): string {
  if (isOverdue(task)) {
    return overdueColor;
  }
  const level = proximityLevel(task);
  if (level === null) {
    return colors.primary['400'];
  }
  return gradedGray(level);
}

function nextRunFontWeight(task: OverviewTask): 'semibold' | 'normal' {
  const level = proximityLevel(task);
  return isOverdue(task) || task.counts.running > 0 || (level ?? 0) >= 0.9
    ? 'semibold'
    : 'normal';
}

// Linear blend from light gray (level 0, far) to near-black (level 1, imminent).
function gradedGray(level: number): string {
  const far = [201, 206, 212];
  const near = [31, 31, 31];
  const [r, g, b] = far.map((f, i) => Math.round(f + (near[i] - f) * level));
  return `rgb(${r}, ${g}, ${b})`;
}

function lastRunText(task: OverviewTask) {
  if (task.worstStatus === 'DORMANT') {
    return <Text color={colors.primary['400']}>-</Text>;
  }

  const lastSuccess = parseDate(task.lastSuccess);
  const lastFailure = parseDate(task.lastFailure);

  if (!lastSuccess && !lastFailure) {
    return <Text color={colors.primary['400']}>-</Text>;
  }

  const lastWasFailure =
    lastFailure !== null && (lastSuccess === null || lastFailure > lastSuccess);
  const date = lastWasFailure ? lastFailure : lastSuccess;

  if (!date) {
    return <Text color={colors.primary['400']}>-</Text>;
  }

  return (
    <Text
      title={dateFormatText(date)}
      color={lastWasFailure ? colors.failed['200'] : colors.success['200']}
      fontWeight={lastWasFailure ? 'semibold' : 'normal'}
    >
      {lastWasFailure ? 'last failure' : 'last success'}{' '}
      {formatDistanceToNowStrict(date, { addSuffix: true })}
    </Text>
  );
}

function parseDate(value: string | null): Date | null {
  return value ? new Date(value) : null;
}

function absoluteTitle(value: string | null): string | undefined {
  return value ? dateFormatText(new Date(value)) : undefined;
}

function isOverdue(task: OverviewTask): boolean {
  return (
    task.counts.running === 0 &&
    !!task.nextExecutionTime &&
    isBefore(new Date(task.nextExecutionTime), new Date())
  );
}

function instancesText(count: number): string {
  return `${count} ${count === 1 ? 'instance' : 'instances'}`;
}

function countPart(count: number, label: string, color: string) {
  if (count === 0) {
    return null;
  }

  return (
    <>
      <Text as="span">·</Text>
      <Text as="span" color={color} fontWeight="semibold">
        {count} {label}
      </Text>
    </>
  );
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

function dotColor(status: OverviewTaskStatus): string {
  if (status === 'DORMANT') {
    return '#cfd6dc';
  }
  if (status === 'SCHEDULED') {
    return colors.primary['400'];
  }
  return statusColors[status];
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
