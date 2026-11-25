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
import { format, isValid } from 'date-fns';

/** A local date-time as the segments of the start-from field, each kept as typed. */
export interface DateTimeParts {
  year: string;
  month: string;
  day: string;
  hour: string;
  minute: string;
  second: string;
}

export const EMPTY_PARTS: DateTimeParts = {
  year: '',
  month: '',
  day: '',
  hour: '',
  minute: '',
  second: '',
};

export function partsOf(date: Date): DateTimeParts {
  const [year, month, day, hour, minute, second] = format(
    date,
    'yyyy MM dd HH mm ss',
  ).split(' ');
  return { year, month, day, hour, minute, second };
}

/**
 * The instant the parts describe in local time, or null when they are incomplete or not a real
 * date (2026.02.30). Time segments left empty count as zero.
 */
export function dateOf(parts: DateTimeParts): Date | null {
  if (!parts.year || !parts.month || !parts.day) {
    return null;
  }
  const n = (v: string) => (v === '' ? 0 : Number(v));
  const date = new Date(
    n(parts.year),
    n(parts.month) - 1,
    n(parts.day),
    n(parts.hour),
    n(parts.minute),
    n(parts.second),
  );
  return isValid(date) &&
    date.getDate() === n(parts.day) &&
    date.getMonth() === n(parts.month) - 1
    ? date
    : null;
}
