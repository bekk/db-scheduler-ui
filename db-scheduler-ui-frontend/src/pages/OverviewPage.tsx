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
  Link,
  Table,
  TableContainer,
  Tbody,
  Td,
  Text,
  Th,
  Thead,
  Tr,
} from '@chakra-ui/react';
import { ArrowForwardIcon } from '@chakra-ui/icons';
import { useQuery } from '@tanstack/react-query';
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
import { Link as RouterLink } from 'react-router-dom';

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

  const recurringTasks = sorted(data.filter((task) => task.recurring === true));
  const customTasks = sorted(data.filter((task) => task.recurring !== true));

  return (
    <Box>
      <Text ml={1} mb={7} fontSize={'3xl'} fontWeight={'semibold'}>
        All tasks
      </Text>
      <TableContainer overflowX="auto">
        <Table
          variant="simple"
          size="md"
          sx={{ borderCollapse: 'separate', borderSpacing: '0 8px' }}
        >
          <Thead>
            <Tr>
              <Th borderBottomColor={colors.primary['300']}>Task</Th>
              <Th width="18%">Next run</Th>
              <Th width="18%">Last run</Th>
              <Th width="10%"></Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading && <MessageRow message="Loading tasks" />}
            {isError && <MessageRow message="Could not load tasks" />}
            {!isLoading && !isError && (
              <>
                <SectionHeader
                  title="Recurring"
                  count={recurringTasks.length}
                />
                {recurringTasks.map((task) => (
                  <OverviewRow key={task.taskName} task={task} />
                ))}
                <SectionHeader
                  title="One-time / custom"
                  count={customTasks.length}
                />
                {customTasks.map((task) => (
                  <OverviewRow key={task.taskName} task={task} />
                ))}
              </>
            )}
          </Tbody>
        </Table>
      </TableContainer>
    </Box>
  );
};

const SectionHeader: React.FC<{ title: string; count: number }> = ({
  title,
  count,
}) => (
  <Tr>
    <Td colSpan={4} borderBottom="none" pb={0} pt={3}>
      <Text
        textTransform="uppercase"
        color={colors.primary['500']}
        fontWeight="bold"
        fontSize="sm"
      >
        {title} · {count}
      </Text>
    </Td>
  </Tr>
);

const OverviewRow: React.FC<{ task: OverviewTask }> = ({ task }) => {
  const drillDownTarget =
    task.instanceCount === 1
      ? `/scheduled/${encodeURIComponent(task.taskName)}`
      : `/scheduled/${encodeURIComponent(task.taskName)}`;
  const drillDownLabel = task.instanceCount === 1 ? 'instance' : 'list';

  return (
    <Tr sx={rowSx(task)}>
      <Td>
        <HStack align="start" spacing={3}>
          <Box
            aria-hidden="true"
            bgColor={dotColor(task.worstStatus)}
            borderRadius="50%"
            flexShrink={0}
            height="0.75rem"
            mt={2}
            width="0.75rem"
          />
          <Box minW={0}>
            <HStack spacing={2}>
              <Text fontWeight="bold">{task.taskName}</Text>
              <Text
                color={statusColors[task.worstStatus]}
                fontWeight="semibold"
              >
                {statusText[task.worstStatus]}
              </Text>
            </HStack>
            <Box color={colors.primary['400']} fontSize="sm">
              {subLine(task)}
            </Box>
          </Box>
        </HStack>
      </Td>
      <Td>
        <Text
          title={absoluteTitle(task.nextExecutionTime)}
          color={isOverdue(task) ? overdueColor : colors.primary['500']}
          fontWeight={
            isOverdue(task) || task.counts.running > 0 ? 'semibold' : 'normal'
          }
        >
          {nextRunText(task)}
        </Text>
      </Td>
      <Td>{lastRunText(task)}</Td>
      <Td textAlign="right">
        {task.instanceCount > 0 && (
          <Link
            as={RouterLink}
            to={drillDownTarget}
            color={colors.dbBlue}
            fontWeight="semibold"
            whiteSpace="nowrap"
          >
            <ArrowForwardIcon aria-hidden="true" /> {drillDownLabel}
          </Link>
        )}
      </Td>
    </Tr>
  );
};

const MessageRow: React.FC<{ message: string }> = ({ message }) => (
  <Tr>
    <Td colSpan={4}>
      <Text color={colors.primary['400']}>{message}</Text>
    </Td>
  </Tr>
);

function sorted(tasks: OverviewTask[]): OverviewTask[] {
  return [...tasks].sort((a, b) => a.taskName.localeCompare(b.taskName));
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

  const base = statusText[task.worstStatus];
  const duration = statusDuration(task);
  const parts = [duration ? `${base} for ${duration}` : base];

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

function lastRunText(task: OverviewTask) {
  const lastSuccess = parseDate(task.lastSuccess);
  const lastFailure = parseDate(task.lastFailure);

  if (!lastSuccess && !lastFailure) {
    return <Text color={colors.primary['400']}>never run</Text>;
  }

  const lastWasFailure =
    lastFailure !== null && (lastSuccess === null || lastFailure > lastSuccess);
  const date = lastWasFailure ? lastFailure : lastSuccess;

  if (!date) {
    return <Text color={colors.primary['400']}>never run</Text>;
  }

  return (
    <Text
      title={dateFormatText(date)}
      color={lastWasFailure ? colors.failed['200'] : colors.success['200']}
      fontWeight="semibold"
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
    return colors.failed['100'];
  }
  if (task.worstStatus === 'RUNNING') {
    return colors.running['100'];
  }
  return colors.primary['100'];
}

function rowBorderColor(task: OverviewTask): string {
  if (task.worstStatus === 'FAILING') {
    return '#f7b8b8';
  }
  if (task.worstStatus === 'RUNNING') {
    return colors.running['200'];
  }
  return colors.primary['300'];
}

function dotColor(status: OverviewTaskStatus): string {
  if (status === 'SCHEDULED') {
    return '#cfd6dc';
  }
  return statusColors[status];
}

function rowSx(task: OverviewTask) {
  return {
    '& > td': {
      bgColor: rowBackground(task),
      borderBottom: '1px solid',
      borderBottomColor: rowBorderColor(task),
      borderTop: '1px solid',
      borderTopColor: rowBorderColor(task),
      py: 4,
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
  };
}
