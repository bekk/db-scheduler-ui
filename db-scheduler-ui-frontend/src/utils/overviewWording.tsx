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
import { HStack, Text } from '@chakra-ui/react';
import {
  formatDistanceStrict,
  formatDistanceToNowStrict,
  isBefore,
} from 'date-fns';
import { OverviewTask, OverviewTaskStatus } from 'src/models/OverviewTask';
import colors from 'src/styles/colors';
import { dateFormatText } from 'src/utils/dateFormatText';
import { isOverdue as executionOverdue } from 'src/utils/overdue';
import { instancesText } from 'src/utils/overviewSummary';

// How a task is described on the Overview row and the task page header.

export const statusText: Record<OverviewTaskStatus, string> = {
  FAILING: 'failing',
  RUNNING: 'running',
  SCHEDULED: 'scheduled',
  DORMANT: 'dormant',
};

export const statusColors: Record<OverviewTaskStatus, string> = {
  FAILING: colors.failed['200'],
  RUNNING: colors.running['300'],
  SCHEDULED: colors.success['200'],
  DORMANT: colors.primary['400'],
};

// Adapts the shared rule in utils/overdue to the aggregate shape of an overview row.
export const overdue = (task: OverviewTask) =>
  executionOverdue(task.nextExecutionTime, task.counts.running > 0);

export function subLine(task: OverviewTask) {
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

  // Spells out what the repeat icon marks. Only recurring is stated: a null `recurring`
  // means the server could not classify the task, not that it is one-time.
  if (task.recurring === true) {
    parts.push('recurring');
  }

  if (task.worstStatus !== 'DORMANT') {
    const base = statusText[task.worstStatus];
    parts.push(duration ? `${base} for ${duration}` : base);
  }

  if (task.recurring !== true) {
    parts.push(instancesText(task.instanceCount));
  }

  return parts.join(' · ');
}

export function statusDuration(task: OverviewTask): string | null {
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

export function nextRunText(task: OverviewTask): string {
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

export function lastRunText(task: OverviewTask) {
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

export function parseDate(value: string | null): Date | null {
  return value ? new Date(value) : null;
}

export function absoluteTitle(value: string | null): string | undefined {
  return value ? dateFormatText(new Date(value)) : undefined;
}

export function countPart(count: number, label: string, color: string) {
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

export function dotColor(status: OverviewTaskStatus): string {
  if (status === 'DORMANT') {
    return '#cfd6dc';
  }
  if (status === 'SCHEDULED') {
    return colors.primary['400'];
  }
  return statusColors[status];
}
