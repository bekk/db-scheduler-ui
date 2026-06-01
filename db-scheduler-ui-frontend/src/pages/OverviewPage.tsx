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
import { RepeatIcon } from '@chakra-ui/icons';
import {
  Box,
  Button,
  Flex,
  Grid,
  GridItem,
  Heading,
  Spinner,
  Text,
} from '@chakra-ui/react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { OverviewTask } from 'src/models/OverviewTask';
import { getOverview, OVERVIEW_QUERY_KEY } from 'src/services/getOverview';
import colors from 'src/styles/colors';
import {
  Cell,
  lastRunCell,
  linkLabel,
  nextRunCell,
  rowBackground,
  sectionLabel,
  statusDotColor,
  subLine,
} from 'src/utils/overviewFormat';

const COLUMNS = 'minmax(0, 1fr) 170px 240px 110px';

export const OverviewPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data, isLoading, isError } = useQuery(
    [OVERVIEW_QUERY_KEY],
    () => getOverview(false),
    { refetchInterval: false }, // navigation-only page: fetch on mount + manual refresh, no poll
  );

  const handleRefresh = () =>
    queryClient.fetchQuery({
      queryKey: [OVERVIEW_QUERY_KEY],
      queryFn: () => getOverview(true),
    });

  const tasks = [...(data ?? [])].sort((a, b) =>
    a.taskName.localeCompare(b.taskName),
  );
  const degraded = tasks.some((task) => task.recurring === null);
  const recurring = tasks.filter((task) => task.recurring === true);
  const others = tasks.filter((task) => task.recurring === false);

  const cellText = (cell: Cell) => (
    <Text
      fontSize="sm"
      color={cell.color}
      fontWeight={cell.bold ? 'bold' : 'normal'}
      title={cell.title}
    >
      {cell.text}
    </Text>
  );

  const renderRow = (task: OverviewTask) => {
    const link = linkLabel(task);
    return (
      <Grid
        key={task.taskName}
        templateColumns={COLUMNS}
        alignItems="center"
        columnGap={4}
        bg={rowBackground(task.worstStatus)}
        px={4}
        py={3}
        mb={2}
        borderRadius="md"
        boxShadow="sm"
      >
        <GridItem minW={0}>
          <Flex align="center" gap={3}>
            <Box
              w="10px"
              h="10px"
              borderRadius="full"
              flexShrink={0}
              bg={statusDotColor(task.worstStatus)}
              aria-hidden
            />
            <Box minW={0}>
              <Text fontWeight="bold" isTruncated>
                {task.taskName}
              </Text>
              <Text fontSize="sm" color={colors.primary[400]}>
                {subLine(task)}
              </Text>
            </Box>
          </Flex>
        </GridItem>
        <GridItem>{cellText(nextRunCell(task))}</GridItem>
        <GridItem>{cellText(lastRunCell(task))}</GridItem>
        <GridItem textAlign="right">
          {link ? (
            <Button
              variant="link"
              color={colors.dbBlue}
              fontWeight="normal"
              onClick={() =>
                navigate(`/${encodeURIComponent(task.taskName)}`)
              }
              aria-label={`Show ${task.taskName} in Scheduled`}
            >
              {link}
            </Button>
          ) : (
            <Text color={colors.primary[400]}>—</Text>
          )}
        </GridItem>
      </Grid>
    );
  };

  const sectionHeader = (label: string, count: number) => (
    <Text
      key={label}
      textTransform="uppercase"
      fontSize="xs"
      fontWeight="bold"
      letterSpacing="wider"
      color={colors.primary[400]}
      mt={6}
      mb={2}
    >
      {label} · {count}
    </Text>
  );

  return (
    <Box>
      <Flex justify="space-between" align="center" mb={6}>
        <Heading as="h1" size="lg">
          All tasks
        </Heading>
        <Button
          leftIcon={<RepeatIcon />}
          variant="outline"
          size="sm"
          onClick={() => handleRefresh()}
          aria-label="Refresh overview"
        >
          Refresh
        </Button>
      </Flex>

      {isLoading && <Spinner aria-label="Loading overview" />}
      {isError && (
        <Text color={colors.failed[200]}>Failed to load overview.</Text>
      )}

      {!isLoading && !isError && (
        <>
          {tasks.length === 0 ? (
            <Text color={colors.primary[400]}>No tasks registered.</Text>
          ) : (
            <>
              <Grid
                templateColumns={COLUMNS}
                columnGap={4}
                px={4}
                pb={2}
                borderBottom="1px solid"
                borderColor={colors.primary[300]}
              >
                {['TASK', 'NEXT RUN', 'LAST RUN', ''].map((heading, index) => (
                  <Text
                    key={index}
                    fontSize="xs"
                    fontWeight="bold"
                    letterSpacing="wider"
                    color={colors.primary[400]}
                  >
                    {heading}
                  </Text>
                ))}
              </Grid>

              {degraded ? (
                <Box mt={2}>{tasks.map(renderRow)}</Box>
              ) : (
                <>
                  {sectionHeader(sectionLabel(true), recurring.length)}
                  {recurring.map(renderRow)}
                  {sectionHeader(sectionLabel(false), others.length)}
                  {others.map(renderRow)}
                </>
              )}
            </>
          )}

          <Text mt={6} fontSize="sm" color={colors.primary[400]}>
            Navigation-only page — no Run / Rerun / Delete. Hover Next/Last run
            for absolute timestamps.
          </Text>
        </>
      )}
    </Box>
  );
};
