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
import { OverviewTask, WorstStatus } from 'src/models/OverviewTask';
import colors from 'src/styles/colors';
import { dateFormatText, relativeTimeText } from 'src/utils/dateFormatText';

export type Cell = {
  text: string;
  color?: string;
  bold?: boolean;
  title?: string;
};

export const sectionLabel = (recurring: boolean): string =>
  recurring ? 'RECURRING' : 'ONE-TIME, DYNAMIC & CUSTOM';

export const statusLabel = (status: WorstStatus): string =>
  status.toLowerCase();

export const statusDotColor = (status: WorstStatus): string => {
  switch (status) {
    case 'FAILING':
      return colors.failed[200];
    case 'RUNNING':
      return colors.running[300];
    default:
      return colors.primary[300]; // scheduled / dormant — colour is never the only signal
  }
};

export const rowBackground = (status: WorstStatus): string => {
  switch (status) {
    case 'FAILING':
      return 'rgba(187, 1, 1, 0.05)';
    case 'RUNNING':
      return 'rgba(80, 104, 246, 0.07)';
    default:
      return colors.primary[100];
  }
};

const durationPhrase = (task: OverviewTask, now: Date): string => {
  switch (task.worstStatus) {
    case 'FAILING':
      return task.lastSuccess
        ? `failing for ${relativeTimeText(new Date(task.lastSuccess), now)}`
        : 'failing';
    case 'RUNNING':
      return task.runningSince
        ? `running for ${relativeTimeText(new Date(task.runningSince), now)}`
        : 'running';
    default:
      return statusLabel(task.worstStatus); // scheduled / dormant
  }
};

/** The grey sub-line beneath the task name. See spec §Sub-line rules. */
export const subLine = (task: OverviewTask, now: Date = new Date()): string => {
  // Recurring (fixed-schedule) rows omit the instance count — one schedule, so it is noise.
  const showCount = task.recurring !== true;
  const phrase = durationPhrase(task, now);

  if (!showCount) {
    return phrase;
  }
  if (task.instanceCount === 0) {
    return 'dormant · 0 instances';
  }
  if (task.instanceCount > 1) {
    const parts: string[] = [];
    if (task.counts.failing > 0) parts.push(`${task.counts.failing} failing`);
    if (task.counts.running > 0) parts.push(`${task.counts.running} running`);
    if (task.counts.scheduled > 0)
      parts.push(`${task.counts.scheduled} scheduled`);
    return `${task.instanceCount} instances · ${parts.join(' · ')}`;
  }
  return `${phrase} · 1 instance`;
};

export const nextRunCell = (task: OverviewTask, now: Date = new Date()): Cell => {
  if (task.worstStatus === 'DORMANT' || task.instanceCount === 0) {
    return { text: '—', color: colors.primary[400] };
  }
  if (!task.nextExecutionTime) {
    return { text: 'running now', color: colors.running[300], bold: true };
  }
  const next = new Date(task.nextExecutionTime);
  const prefix = task.instanceCount > 1 ? 'soonest ' : '';
  const title = dateFormatText(next);
  if (next.getTime() > now.getTime()) {
    return { text: `${prefix}in ${relativeTimeText(next, now)}`, title };
  }
  return {
    text: `${prefix}due ${relativeTimeText(next, now)} ago`,
    color: colors.warning,
    title,
  };
};

export const lastRunCell = (task: OverviewTask, now: Date = new Date()): Cell => {
  const lastSuccess = task.lastSuccess ? new Date(task.lastSuccess) : null;
  const lastFailure = task.lastFailure ? new Date(task.lastFailure) : null;

  if (!lastSuccess && !lastFailure) {
    return { text: 'never run', color: colors.primary[400] };
  }
  if (lastFailure && (!lastSuccess || lastFailure.getTime() > lastSuccess.getTime())) {
    return {
      text: `last failure ${relativeTimeText(lastFailure, now)} ago`,
      color: colors.failed[200],
      title: dateFormatText(lastFailure),
    };
  }
  // lastSuccess is non-null here.
  const success = lastSuccess as Date;
  return {
    text: `last success ${relativeTimeText(success, now)} ago`,
    color: colors.success[200],
    title: dateFormatText(success),
  };
};

/** `→ instance` (single), `→ list` (many), or null for a dormant row (no link). */
export const linkLabel = (task: OverviewTask): string | null => {
  if (task.instanceCount === 0) {
    return null;
  }
  return task.instanceCount === 1 ? '→ instance' : '→ list';
};
