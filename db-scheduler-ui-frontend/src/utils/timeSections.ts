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
  addDays,
  addHours,
  differenceInCalendarDays,
  format,
  isBefore,
  startOfDay,
} from 'date-fns';
import { ExecutionRow } from 'src/models/ExecutionPage';

export interface TimeSection {
  key: string;
  label: string;
  rows: ExecutionRow[];
}

/**
 * Splits rows already in execution-time order into consecutive sections a reader can scan:
 * running now, overdue, next hour, later today, tomorrow, then one per later day.
 */
export function timeSections(rows: ExecutionRow[], now: Date): TimeSection[] {
  const sections: TimeSection[] = [];
  for (const row of rows) {
    const { key, label } = sectionOf(row, now);
    const last = sections[sections.length - 1];
    if (last && last.key === key) {
      last.rows.push(row);
    } else {
      sections.push({ key, label, rows: [row] });
    }
  }
  return sections;
}

function sectionOf(
  row: ExecutionRow,
  now: Date,
): { key: string; label: string } {
  const time = new Date(row.executionTime);
  if (row.picked) {
    return { key: 'running', label: 'Running now' };
  }
  if (isBefore(time, now)) {
    return { key: 'overdue', label: 'Overdue' };
  }
  if (isBefore(time, addHours(now, 1))) {
    return { key: 'hour', label: 'Next hour' };
  }
  const days = differenceInCalendarDays(time, now);
  if (days === 0) {
    return { key: 'today', label: 'Later today' };
  }
  if (days === 1) {
    return { key: 'tomorrow', label: 'Tomorrow' };
  }
  const day = startOfDay(time);
  return {
    key: day.toISOString(),
    label: isBefore(day, addDays(startOfDay(now), 7))
      ? format(day, 'EEEE d MMM')
      : format(day, 'd MMM yyyy'),
  };
}
