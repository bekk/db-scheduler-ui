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
import { OverviewTask } from 'src/models/OverviewTask';
import { matchesFilter, OverviewFilterKey } from 'src/utils/overviewFilters';

/**
 * Every stat is a pair: how many **tasks** are in that state, and how many **instances**
 * sit behind them. The task count leads because it is what the table lists and what a
 * filter leaves behind — "4 failing" and "4 rows" have to mean the same thing.
 */
export interface OverviewStat {
  tasks: number;
  instances: number;
}

// One stat per filter, plus the unfiltered total. Typing it as a Record over the filter
// keys means a new filter cannot be added without a stat to go with it.
export type OverviewSummary = Record<OverviewFilterKey, OverviewStat> & {
  all: OverviewStat;
};

// Derived from the same /tasks/overview payload the table renders, so the strip needs no
// endpoint of its own. It always totals the full task set: filtering narrows the table,
// never these numbers — the strip is the "is anything wrong" glance.
export const summarizeOverview = (tasks: OverviewTask[]): OverviewSummary => ({
  all: {
    tasks: tasks.length,
    instances: sumBy(tasks, (task) => task.instanceCount),
  },
  failing: stat(tasks, 'failing'),
  running: stat(tasks, 'running'),
  scheduled: stat(tasks, 'scheduled'),
});

export const instancesText = (count: number): string =>
  `${count} ${count === 1 ? 'instance' : 'instances'}`;

const stat = (tasks: OverviewTask[], key: OverviewFilterKey): OverviewStat => ({
  tasks: tasks.filter((task) => matchesFilter(task, key)).length,
  instances: sumBy(tasks, (task) => task.counts[key]),
});

const sumBy = (
  tasks: OverviewTask[],
  value: (task: OverviewTask) => number,
): number => tasks.reduce((total, task) => total + value(task), 0);
