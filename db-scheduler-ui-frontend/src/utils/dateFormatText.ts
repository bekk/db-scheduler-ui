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
import { differenceInSeconds, format } from 'date-fns';

export function dateFormatText(date: Date) {
  return format(date, 'dd. MMM yy, H:mm:ss');
}

/**
 * Compact relative magnitude between `date` and `now` — the largest non-zero unit among
 * seconds/minutes/hours/days, e.g. `12s`, `5m`, `1h`, `3d`. Sign-agnostic; callers add the
 * `in …` / `… ago` / `due … ago` framing.
 */
export function relativeTimeText(date: Date, now: Date = new Date()): string {
  const seconds = Math.abs(differenceInSeconds(now, date));
  if (seconds < 60) return `${seconds}s`;
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h`;
  const days = Math.floor(hours / 24);
  return `${days}d`;
}
