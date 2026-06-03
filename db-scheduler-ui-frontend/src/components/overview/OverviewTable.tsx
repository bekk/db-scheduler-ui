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
import { Grid, GridItem, Text } from '@chakra-ui/react';
import React, { useMemo } from 'react';
import { OverviewSection } from 'src/components/overview/OverviewSection';
import { OVERVIEW_GRID_COLUMNS } from 'src/components/overview/OverviewRow';
import { OverviewTask } from 'src/models/Overview';
import colors from 'src/styles/colors';

const byTaskName = (a: OverviewTask, b: OverviewTask) =>
  a.taskName.localeCompare(b.taskName);

const ColumnHeader: React.FC<{ label: string; align?: 'left' | 'right' }> = ({
  label,
  align = 'left',
}) => (
  <Text
    fontSize="xs"
    fontWeight="bold"
    letterSpacing="wide"
    color={colors.primary['400']}
    textAlign={align}
  >
    {label}
  </Text>
);

export const OverviewTable: React.FC<{ tasks: OverviewTask[] }> = ({
  tasks,
}) => {
  // Stable, strictly-alphabetical positions — never reorder by status. One render timestamp so
  // every relative duration in this pass is computed against the same "now".
  const now = Date.now();

  const { recurring, oneTime } = useMemo(() => {
    const recurringTasks = tasks
      .filter((task) => task.recurring === true)
      .sort(byTaskName);
    const oneTimeTasks = tasks
      .filter((task) => task.recurring !== true)
      .sort(byTaskName);
    return { recurring: recurringTasks, oneTime: oneTimeTasks };
  }, [tasks]);

  return (
    <>
      <Grid
        templateColumns={OVERVIEW_GRID_COLUMNS}
        gap={4}
        px={5}
        pb={3}
        borderBottomWidth={1}
        borderColor={colors.primary['300']}
        mb={4}
      >
        <GridItem>
          <ColumnHeader label="TASK" />
        </GridItem>
        <GridItem>
          <ColumnHeader label="NEXT RUN" />
        </GridItem>
        <GridItem>
          <ColumnHeader label="LAST RUN" />
        </GridItem>
        <GridItem />
      </Grid>

      <OverviewSection title="RECURRING" tasks={recurring} now={now} />
      <OverviewSection title="ONE-TIME / CUSTOM" tasks={oneTime} now={now} />
    </>
  );
};
