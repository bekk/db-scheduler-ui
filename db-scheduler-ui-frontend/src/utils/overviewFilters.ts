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
import { OverviewTask, OverviewTaskCounts } from 'src/models/OverviewTask';

// A filter is a state a task can be in, named after the count that defines it. Deriving the
// key from the payload's own counts keeps the strip, the filters and the server in step.
export type OverviewFilterKey = keyof OverviewTaskCounts;

// Canonical order: the strip renders in it and the URL encodes filters in it, so the same
// set of filters always produces the same link.
export const OVERVIEW_FILTER_KEYS: OverviewFilterKey[] = [
  'failing',
  'running',
  'scheduled',
];

// The single definition of "this task is failing / running / scheduled". Both the strip's
// task counts and the table's filtering go through it, so "4 failing" and 4 rows can never
// come to mean different things.
export const matchesFilter = (
  task: OverviewTask,
  key: OverviewFilterKey,
): boolean => task.counts[key] > 0;

// Filters combine with AND. The overview loads every task in one go, so this stays in the
// browser — no round-trip, and the filters combine freely (see the spec's "why client-side").
export const applyOverviewFilters = (
  tasks: OverviewTask[],
  activeFilters: OverviewFilterKey[],
): OverviewTask[] =>
  tasks.filter((task) =>
    activeFilters.every((key) => matchesFilter(task, key)),
  );
