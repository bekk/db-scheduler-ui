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
import { OverviewTask, WorstStatus } from 'src/models/Overview';
import colors from 'src/styles/colors';
import { dateFormatText } from 'src/utils/dateFormatText';

/** A cell's rendered text plus optional emphasis colour and an absolute-timestamp hover title. */
export interface CellText {
  text: string;
  color?: string;
  bold?: boolean;
  muted?: boolean;
  title?: string;
}

/** Compact, human duration: `6s`, `26m`, `2h`, `4d`. Always the single largest unit. */
export function formatCompactDuration(ms: number): string {
  const seconds = Math.max(0, Math.floor(ms / 1000));
  if (seconds < 60) return `${seconds}s`;
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h`;
  return `${Math.floor(hours / 24)}d`;
}

/** Status dot colour + one-word label. Colour is never the only signal — the label carries it too. */
export function statusVisual(status: WorstStatus): {
  label: string;
  dotColor: string;
} {
  switch (status) {
    case 'FAILING':
      return { label: 'failing', dotColor: colors.failed['200'] };
    case 'RUNNING':
      return { label: 'running', dotColor: colors.running['300'] };
    case 'SCHEDULED':
      return { label: 'scheduled', dotColor: colors.primary['300'] };
    case 'DORMANT':
      return { label: 'dormant', dotColor: colors.primary['300'] };
  }
}

function pluralInstances(n: number): string {
  return `${n} instance${n === 1 ? '' : 's'}`;
}

/**
 * Sub-line beneath the task name. Rules (spec §Sub-line):
 * - failing: `failing for <now − lastSuccess>`; no duration when never succeeded.
 * - running: `running for <now − nextExecutionTime>` (proxy for run time).
 * - scheduled / dormant: status word only.
 * - recurring rows omit the instance count; one-time/custom rows append `· N instance(s)`.
 * - multi-instance: per-state breakdown instead of a single duration.
 */
export function buildSubLine(task: OverviewTask, now: number): string {
  const { worstStatus, counts, instanceCount, recurring } = task;

  if (instanceCount > 1) {
    // These three counts partition the instances and always sum to instanceCount, but note the
    // DB-summary semantics: `running` = picked (which includes any failing instance currently being
    // retried), `failing` = un-picked failing, `scheduled` = un-picked healthy. The worst-status
    // dot keys off maxConsecutiveFailures (any failure, picked or not), so a row can correctly show
    // a FAILING dot while this breakdown shows `0 failing` if every failing instance is mid-retry.
    // The summary doesn't expose the picked-failing count, so we can't split it out further.
    const parts = [pluralInstances(instanceCount)];
    if (counts.failing > 0) parts.push(`${counts.failing} failing`);
    if (counts.running > 0) parts.push(`${counts.running} running`);
    if (counts.scheduled > 0) parts.push(`${counts.scheduled} scheduled`);
    return parts.join(' · ');
  }

  let base: string;
  switch (worstStatus) {
    case 'FAILING':
      base = task.lastSuccess
        ? `failing for ${formatCompactDuration(now - Date.parse(task.lastSuccess))}`
        : 'failing';
      break;
    case 'RUNNING':
      base = task.nextExecutionTime
        ? `running for ${formatCompactDuration(now - Date.parse(task.nextExecutionTime))}`
        : 'running';
      break;
    case 'SCHEDULED':
      base = 'scheduled';
      break;
    case 'DORMANT':
      base = 'dormant';
      break;
  }

  // Recurring rows have exactly one schedule, so the count is noise; omit it.
  if (recurring === true) return base;
  return `${base} · ${pluralInstances(instanceCount)}`;
}

/**
 * NEXT RUN column (spec §Next-run): relative future time, `running now`, overdue `due X ago`
 * (warning), or `—` for dormant. Multi-instance is prefixed `soonest`.
 */
export function formatNextRun(task: OverviewTask, now: number): CellText {
  if (task.worstStatus === 'DORMANT' || task.nextExecutionTime === null) {
    return { text: '—', muted: true };
  }

  const multi = task.instanceCount > 1;
  const next = Date.parse(task.nextExecutionTime);
  const title = dateFormatText(new Date(next));

  if (next >= now) {
    const rel = `in ${formatCompactDuration(next - now)}`;
    return { text: multi ? `soonest ${rel}` : rel, title };
  }

  // nextExecutionTime is in the past. A picked (running) execution keeps its execution_time at
  // firing time, so a past timestamp on a row that has a running instance is that instance — show
  // liveness, not a false "due X ago" warning (the run-duration lives in the sub-line). Only when
  // nothing is picked is a past schedule genuinely overdue.
  if (task.counts.running > 0) {
    return { text: 'running now', color: colors.running['300'], bold: true };
  }

  return {
    text: `due ${formatCompactDuration(now - next)} ago`,
    color: colors.warning,
    bold: true,
    title,
  };
}

/**
 * LAST RUN column (spec §Last-run): the most-recent of last-success (green) / last-failure (red)
 * across instances, relative; `never run` when neither exists.
 */
export function formatLastRun(task: OverviewTask, now: number): CellText {
  const success = task.lastSuccess ? Date.parse(task.lastSuccess) : null;
  const failure = task.lastFailure ? Date.parse(task.lastFailure) : null;

  if (success === null && failure === null) {
    return { text: 'never run', muted: true };
  }

  if (failure !== null && (success === null || failure >= success)) {
    return {
      text: `last failure ${formatCompactDuration(now - failure)} ago`,
      color: colors.failed['200'],
      title: dateFormatText(new Date(failure)),
    };
  }

  return {
    text: `last success ${formatCompactDuration(now - (success as number))} ago`,
    color: colors.success['200'],
    title: dateFormatText(new Date(success as number)),
  };
}

/** Drill-down link target into the Scheduled list filtered by this task name. */
export interface DrillDown {
  label: string;
  to: string | null;
}

export function drillDown(task: OverviewTask): DrillDown {
  if (task.instanceCount === 0) return { label: '—', to: null };
  return {
    label: task.instanceCount === 1 ? '→ instance' : '→ list',
    to: `/${encodeURIComponent(task.taskName)}`,
  };
}

/** Faint row tint for at-a-glance health, mirroring the status colour without relying on it. */
export function rowBackground(status: WorstStatus): string {
  if (status === 'FAILING') return '#FDF3F3';
  if (status === 'RUNNING') return '#F1F5FD';
  return colors.primary['100'];
}
