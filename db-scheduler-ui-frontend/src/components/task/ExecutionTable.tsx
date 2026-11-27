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
import { ChevronDownIcon, ChevronUpIcon } from '@chakra-ui/icons';
import { Box, Button, Grid, HStack, Text } from '@chakra-ui/react';
import { formatDistanceToNowStrict, isBefore } from 'date-fns';
import { useEffect } from 'react';
import { ExecutionRow } from 'src/models/ExecutionPage';
import { InstanceStatus } from 'src/models/InstanceDetail';
import colors from 'src/styles/colors';
import { dateFormatText } from 'src/utils/dateFormatText';
import { isOverdue, overdueColor } from 'src/utils/overdue';
import { timeSections } from 'src/utils/timeSections';

const statusLabel: Record<InstanceStatus, string> = {
  FAILED: 'Failed',
  RUNNING: 'Running',
  SCHEDULED: 'Scheduled',
};

const statusColor: Record<InstanceStatus, string> = {
  FAILED: colors.failed['200'],
  RUNNING: colors.running['300'],
  SCHEDULED: colors.primary['400'],
};

// The Overview's row tints, so a failing instance looks like a failing task.
const rowTint: Record<
  InstanceStatus,
  { bg: string; border: string; hover: string }
> = {
  FAILED: { bg: '#fcf3f3', border: '#f3dede', hover: '#f7e8e8' },
  RUNNING: { bg: '#f6f8fd', border: '#e7edf9', hover: '#eef2fc' },
  SCHEDULED: {
    bg: colors.primary['100'],
    border: colors.primary['300'],
    hover: colors.primary['200'],
  },
};

interface ExecutionTableProps {
  rows: ExecutionRow[];
  selected: string | null;
  onSelect: (id: string) => void;
  /** Every row has the same status, so the column would only repeat the tab. */
  hideStatus: boolean;
  /** Failing rows wait for a retry; label the time column for what it is. */
  nextRunLabel: string;
  /** The expanded row's details. */
  renderDetails: (id: string) => React.ReactNode;
}

/** DOM id of a row, for scrolling an expanded row into view. */
const rowDomId = (id: string) => `execution-${id}`;

export const ExecutionTable: React.FC<ExecutionTableProps> = ({
  rows,
  selected,
  onSelect,
  hideStatus,
  nextRunLabel,
  renderDetails,
}) => {
  const columns = hideStatus
    ? 'minmax(16rem, 2.4fr) 1.2fr 1.3fr 4.5rem 6rem'
    : 'minmax(16rem, 2.4fr) 0.8fr 1.2fr 1.3fr 4.5rem 6rem';
  const sections = timeSections(rows, new Date());

  // Bring a newly expanded row into view; its details push the rows below it down.
  useEffect(() => {
    if (selected) {
      document
        .getElementById(rowDomId(selected))
        ?.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
    }
  }, [selected]);

  return (
    <Box>
      <Grid
        templateColumns={columns}
        gap={3}
        px={4}
        pb={2}
        borderBottom="1px solid"
        borderBottomColor={colors.primary['300']}
        fontSize="xs"
        fontWeight="bold"
        textTransform="uppercase"
        letterSpacing="wider"
        color={colors.primary['400']}
      >
        <Text>Instance</Text>
        {!hideStatus && <Text>Status</Text>}
        <Text>{nextRunLabel}</Text>
        <Text>Last run</Text>
        <Text textAlign="right">Fails</Text>
        <span />
      </Grid>
      {sections.map((section) => (
        <Box key={section.key} mt={4}>
          <Text
            fontSize="xs"
            fontWeight="bold"
            textTransform="uppercase"
            letterSpacing="wider"
            color={
              section.key === 'overdue' ? overdueColor : colors.primary['500']
            }
            mb={2}
          >
            {section.label}
            <Text as="span" fontWeight="normal" color={colors.primary['400']}>
              {' '}
              · {section.rows.length}
            </Text>
          </Text>
          {section.rows.map((row) => {
            const tint = rowTint[row.status];
            const isSelected = row.taskInstance === selected;
            return (
              <Box
                key={row.taskInstance}
                id={rowDomId(row.taskInstance)}
                mb={2}
                bgColor={isSelected ? 'white' : tint.bg}
                border="1px solid"
                borderColor={isSelected ? colors.running['300'] : tint.border}
                boxShadow={isSelected ? 'md' : undefined}
                borderRadius="8px"
                overflow="hidden"
              >
                <Grid
                  templateColumns={columns}
                  gap={3}
                  alignItems="center"
                  px={4}
                  py={3}
                  bgColor={isSelected ? colors.running['100'] : undefined}
                  cursor="pointer"
                  transition="background-color 0.12s ease"
                  _hover={{
                    bgColor: isSelected ? colors.running['100'] : tint.hover,
                  }}
                  onClick={() => onSelect(row.taskInstance)}
                  title={isSelected ? 'Hide details' : 'Show details'}
                >
                  <Box minW={0}>
                    <Text
                      fontFamily="mono"
                      fontSize="sm"
                      fontWeight="semibold"
                      wordBreak="break-all"
                    >
                      {row.taskInstance}
                    </Text>
                    {row.taskDataPreview && (
                      <DataPreview preview={row.taskDataPreview} />
                    )}
                  </Box>
                  {!hideStatus && (
                    <Text
                      color={statusColor[row.status]}
                      fontWeight="semibold"
                      fontSize="sm"
                    >
                      {statusLabel[row.status]}
                    </Text>
                  )}
                  <NextRun row={row} />
                  <LastRun row={row} />
                  <Text
                    textAlign="right"
                    fontWeight="bold"
                    color={
                      row.consecutiveFailures > 0
                        ? colors.failed['200']
                        : colors.primary['300']
                    }
                  >
                    {row.consecutiveFailures > 0
                      ? `${row.consecutiveFailures}×`
                      : '—'}
                  </Text>
                  <Box textAlign="right">
                    <Button
                      size="xs"
                      variant="ghost"
                      color={colors.running['300']}
                      rightIcon={
                        isSelected ? <ChevronUpIcon /> : <ChevronDownIcon />
                      }
                      aria-expanded={isSelected}
                      aria-label={`${
                        isSelected ? 'Hide' : 'Show'
                      } details for ${row.taskInstance}`}
                      onClick={(event) => {
                        event.stopPropagation();
                        onSelect(row.taskInstance);
                      }}
                    >
                      Details
                    </Button>
                  </Box>
                </Grid>
                {isSelected && (
                  <Box
                    px={6}
                    pb={5}
                    borderTop="1px solid"
                    borderTopColor={colors.primary['200']}
                  >
                    {renderDetails(row.taskInstance)}
                  </Box>
                )}
              </Box>
            );
          })}
        </Box>
      ))}
    </Box>
  );
};

// A flat JSON object reads better as key/value chips; anything else (including a preview the
// server cut short, which no longer parses) is shown as text.
const DataPreview: React.FC<{ preview: string }> = ({ preview }) => {
  const entries = flatEntries(preview);
  if (!entries) {
    return (
      <Text
        fontFamily="mono"
        fontSize="xs"
        color={colors.primary['400']}
        noOfLines={1}
        title={preview}
      >
        {preview}
      </Text>
    );
  }
  return (
    <HStack spacing={1} mt={1} flexWrap="wrap" title={preview}>
      {entries.slice(0, 4).map(([key, value]) => (
        <Text
          key={key}
          as="span"
          fontSize="xs"
          bgColor={colors.primary['200']}
          borderRadius="4px"
          px={1.5}
          color={colors.primary['500']}
        >
          <Text as="span" fontWeight="semibold" color={colors.primary['400']}>
            {key}
          </Text>{' '}
          {value}
        </Text>
      ))}
    </HStack>
  );
};

function flatEntries(preview: string): [string, string][] | null {
  try {
    const parsed: unknown = JSON.parse(preview);
    if (
      parsed === null ||
      typeof parsed !== 'object' ||
      Array.isArray(parsed)
    ) {
      return null;
    }
    const entries = Object.entries(parsed as Record<string, unknown>);
    if (entries.some(([, v]) => v !== null && typeof v === 'object')) {
      return null;
    }
    return entries.map(([k, v]) => [k, String(v)]);
  } catch {
    return null;
  }
}

const NextRun: React.FC<{ row: ExecutionRow }> = ({ row }) => {
  if (row.picked) {
    return (
      <Text color={colors.running['300']} fontWeight="semibold" fontSize="sm">
        running now
      </Text>
    );
  }
  const date = new Date(row.executionTime);
  const distance = formatDistanceToNowStrict(date);
  const overdue = isOverdue(row.executionTime, false);
  return (
    <Text
      fontSize="sm"
      title={dateFormatText(date)}
      color={overdue ? overdueColor : colors.primary['500']}
      fontWeight={overdue ? 'semibold' : 'normal'}
    >
      {isBefore(date, new Date()) ? `due ${distance} ago` : `in ${distance}`}
    </Text>
  );
};

const LastRun: React.FC<{ row: ExecutionRow }> = ({ row }) => {
  const success = row.lastSuccess ? new Date(row.lastSuccess) : null;
  const failure = row.lastFailure ? new Date(row.lastFailure) : null;
  const latest =
    failure && (!success || failure > success)
      ? { date: failure, failed: true }
      : success
      ? { date: success, failed: false }
      : null;

  if (!latest) {
    return (
      <Text fontSize="sm" color={colors.primary['400']}>
        never run
      </Text>
    );
  }
  return (
    <Text
      fontSize="sm"
      title={dateFormatText(latest.date)}
      color={latest.failed ? colors.failed['200'] : colors.success['200']}
      fontWeight={latest.failed ? 'semibold' : 'normal'}
    >
      {latest.failed ? '✕ failed' : '✓ succeeded'}{' '}
      {formatDistanceToNowStrict(latest.date, { addSuffix: true })}
    </Text>
  );
};
