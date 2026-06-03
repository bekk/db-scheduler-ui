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
import { Box, Grid, GridItem, Link, Text } from '@chakra-ui/react';
import React from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { OverviewTask } from 'src/models/Overview';
import colors from 'src/styles/colors';
import {
  buildSubLine,
  CellText,
  drillDown,
  formatLastRun,
  formatNextRun,
  rowBackground,
  statusVisual,
} from 'src/utils/overviewFormat';

export const OVERVIEW_GRID_COLUMNS = '1fr 170px 230px 90px';

const Cell: React.FC<{ cell: CellText }> = ({ cell }) => (
  <Text
    fontSize="sm"
    fontWeight={cell.bold ? 'semibold' : 'normal'}
    color={cell.muted ? colors.primary['400'] : cell.color ?? colors.primary['600']}
    title={cell.title}
  >
    {cell.text}
  </Text>
);

interface OverviewRowProps {
  task: OverviewTask;
  now: number;
}

export const OverviewRow: React.FC<OverviewRowProps> = ({ task, now }) => {
  const { label, dotColor } = statusVisual(task.worstStatus);
  const subLine = buildSubLine(task, now);
  const nextRun = formatNextRun(task, now);
  const lastRun = formatLastRun(task, now);
  const link = drillDown(task);

  return (
    <Grid
      templateColumns={OVERVIEW_GRID_COLUMNS}
      alignItems="center"
      gap={4}
      px={5}
      py={3}
      mb={2}
      borderWidth={1}
      borderColor={colors.primary['300']}
      borderRadius={6}
      backgroundColor={rowBackground(task.worstStatus)}
    >
      <GridItem>
        <Box display="flex" alignItems="center" gap={3}>
          <Box
            as="span"
            aria-hidden
            width="10px"
            height="10px"
            minWidth="10px"
            borderRadius="full"
            backgroundColor={dotColor}
          />
          <Box>
            <Text fontWeight="bold" fontSize="md" color={colors.primary['900']}>
              {task.taskName}
            </Text>
            <Text fontSize="sm" color={colors.primary['400']}>
              <Box as="span" srOnly>
                {label}:{' '}
              </Box>
              {subLine}
            </Text>
          </Box>
        </Box>
      </GridItem>

      <GridItem>
        <Cell cell={nextRun} />
      </GridItem>

      <GridItem>
        <Cell cell={lastRun} />
      </GridItem>

      <GridItem textAlign="right">
        {link.to ? (
          <Link
            as={RouterLink}
            to={link.to}
            color={colors.dbBlue}
            fontSize="sm"
            fontWeight="medium"
            aria-label={`Open ${task.taskName} in the Scheduled list`}
          >
            {link.label}
          </Link>
        ) : (
          <Text fontSize="sm" color={colors.primary['400']}>
            {link.label}
          </Text>
        )}
      </GridItem>
    </Grid>
  );
};
