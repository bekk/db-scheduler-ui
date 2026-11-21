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
  Table,
  TableContainer,
  Tbody,
  Td,
  Text,
  Th,
  Thead,
  Tr,
} from '@chakra-ui/react';
import { formatDistanceToNowStrict, isBefore } from 'date-fns';
import { InstanceStatus } from 'src/models/InstanceDetail';
import { ExecutionRow } from 'src/models/ExecutionPage';
import colors from 'src/styles/colors';
import { dateFormatText } from 'src/utils/dateFormatText';
import { isOverdue, overdueColor } from 'src/utils/overdue';

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

export const ExecutionTable: React.FC<{
  rows: ExecutionRow[];
  selected: string | null;
  onSelect: (id: string) => void;
}> = ({ rows, selected, onSelect }) => (
  <TableContainer overflowX="auto" whiteSpace="normal">
    <Table size="sm" variant="simple">
      <Thead>
        <Tr>
          <Th>Instance</Th>
          <Th>Status</Th>
          <Th>Next run</Th>
          <Th>Last run</Th>
          <Th isNumeric>Failures</Th>
        </Tr>
      </Thead>
      <Tbody>
        {rows.map((row) => (
          <Tr
            key={row.taskInstance}
            cursor="pointer"
            bgColor={
              row.taskInstance === selected ? colors.running['100'] : undefined
            }
            _hover={{
              bgColor:
                row.taskInstance === selected
                  ? colors.running['100']
                  : colors.primary['200'],
            }}
            onClick={() => onSelect(row.taskInstance)}
          >
            <Td maxW="32rem">
              <Text fontFamily="mono" fontSize="sm" wordBreak="break-all">
                {row.taskInstance}
              </Text>
              {row.taskDataPreview && (
                <Text
                  fontFamily="mono"
                  fontSize="xs"
                  color={colors.primary['400']}
                  noOfLines={1}
                  title={row.taskDataPreview}
                >
                  {row.taskDataPreview}
                </Text>
              )}
            </Td>
            <Td>
              <Text color={statusColor[row.status]} fontWeight="semibold">
                {statusLabel[row.status]}
              </Text>
            </Td>
            <Td>
              <NextRun row={row} />
            </Td>
            <Td>
              <LastRun row={row} />
            </Td>
            <Td isNumeric>
              {row.consecutiveFailures > 0 ? (
                <Text color={colors.failed['200']} fontWeight="bold">
                  {row.consecutiveFailures}
                </Text>
              ) : (
                <Text color={colors.primary['400']}>—</Text>
              )}
            </Td>
          </Tr>
        ))}
      </Tbody>
    </Table>
  </TableContainer>
);

const NextRun: React.FC<{ row: ExecutionRow }> = ({ row }) => {
  if (row.picked) {
    return (
      <Text color={colors.running['300']} fontWeight="semibold">
        running now
      </Text>
    );
  }
  const date = new Date(row.executionTime);
  const distance = formatDistanceToNowStrict(date);
  const overdue = isOverdue(row.executionTime, false);
  return (
    <Text
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
    return <Text color={colors.primary['400']}>never run</Text>;
  }
  return (
    <Text
      title={dateFormatText(latest.date)}
      color={latest.failed ? colors.failed['200'] : colors.success['200']}
      fontWeight={latest.failed ? 'semibold' : 'normal'}
    >
      {latest.failed ? '✕' : '✓'}{' '}
      {formatDistanceToNowStrict(latest.date, { addSuffix: true })}
    </Text>
  );
};
