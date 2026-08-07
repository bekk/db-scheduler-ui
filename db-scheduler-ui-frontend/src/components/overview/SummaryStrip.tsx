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
import { Box, Flex, Text, Wrap, WrapItem } from '@chakra-ui/react';
import { OverviewFilterKey } from 'src/utils/overviewFilters';
import {
  instancesText,
  OverviewStat,
  OverviewSummary,
} from 'src/utils/overviewSummary';
import colors from 'src/styles/colors';

interface SummaryStripProps {
  summary: OverviewSummary;
  activeFilters: OverviewFilterKey[];
  shownCount: number;
  onToggleFilter: (key: OverviewFilterKey) => void;
  onClearFilters: () => void;
}

interface FilterCard {
  key: OverviewFilterKey;
  label: string;
  tone: string;
  hint: string;
}

const filterCards: FilterCard[] = [
  {
    key: 'failing',
    label: 'Failing',
    tone: colors.failed['200'],
    hint: 'Show only tasks with failures',
  },
  {
    key: 'running',
    label: 'Running',
    tone: colors.running['300'],
    hint: 'Show only tasks running right now',
  },
  {
    key: 'scheduled',
    label: 'Scheduled',
    tone: colors.primary['600'],
    hint: 'Show only tasks with queued work',
  },
];

/**
 * Totals for the whole task set — always, regardless of what is filtered. Every cell is
 * also the control that filters the table down to it, so counting and filtering live in one
 * row. `Tasks` is the unfiltered view and reads as selected whenever nothing else is: a
 * highlighted cell on arrival is what tells you the row can be clicked at all.
 */
export const SummaryStrip: React.FC<SummaryStripProps> = ({
  summary,
  activeFilters,
  shownCount,
  onToggleFilter,
  onClearFilters,
}) => (
  <Flex align="center" justify="space-between" gap={4} wrap="wrap">
    <Wrap spacing={3} role="group" aria-label="Task totals">
      <WrapItem>
        <StatCard
          stat={summary.all}
          label="Tasks"
          tone={colors.primary['600']}
          hint="Show all tasks"
          active={activeFilters.length === 0}
          onSelect={onClearFilters}
        />
      </WrapItem>
      {filterCards.map((card) => (
        <WrapItem key={card.key}>
          <StatCard
            stat={summary[card.key]}
            label={card.label}
            tone={card.tone}
            hint={card.hint}
            active={activeFilters.includes(card.key)}
            onSelect={() => onToggleFilter(card.key)}
          />
        </WrapItem>
      ))}
    </Wrap>
    {activeFilters.length > 0 && (
      <Text fontSize="sm" color={colors.primary['400']}>
        Showing {shownCount} of {summary.all.tasks}{' '}
        {summary.all.tasks === 1 ? 'task' : 'tasks'}
      </Text>
    )}
  </Flex>
);

interface StatCardProps {
  stat: OverviewStat;
  label: string;
  tone: string;
  hint: string;
  active: boolean;
  onSelect: () => void;
}

const StatCard: React.FC<StatCardProps> = ({
  stat,
  label,
  tone,
  hint,
  active,
  onSelect,
}) => {
  // Zero is calm, not alarming: the alert colour is reserved for something to act on.
  const valueColor = stat.tasks > 0 ? tone : colors.primary['400'];
  // Selecting a stat that matches no task could only empty the table, so it is inert then
  // — unless it is the current selection, which must stay clickable to switch off.
  const selectable = stat.tasks > 0 || active;

  return (
    <Box
      minW="8.5rem"
      bgColor={colors.primary['100']}
      border="1px solid"
      // The outline follows the stat's own colour, not the muted zero shade: a filter that
      // has been switched on must look switched on even once its count drops to zero.
      borderColor={active ? tone : colors.primary['300']}
      boxShadow={active ? `inset 0 0 0 1px ${tone}` : undefined}
      borderRadius="8px"
      px={4}
      py={3}
      transition="border-color 0.12s ease"
      {...(selectable && {
        as: 'button',
        type: 'button',
        textAlign: 'left',
        cursor: 'pointer',
        title: hint,
        'aria-pressed': active,
        'aria-label': `${label}: ${stat.tasks}. ${hint}`,
        onClick: onSelect,
        _hover: { borderColor: tone },
      })}
    >
      <Text
        fontSize="2xl"
        fontWeight="bold"
        lineHeight="1.2"
        color={valueColor}
      >
        {stat.tasks}
      </Text>
      <Text
        fontSize="xs"
        fontWeight="bold"
        textTransform="uppercase"
        letterSpacing="wider"
        color={active ? tone : colors.primary['400']}
      >
        {label}
      </Text>
      {/* The instance total behind the task count: "4 failing" is 4 rows, but it may be
          hundreds of executions, and that difference is the whole story on a group task. */}
      <Text fontSize="xs" color={colors.primary['400']} minH="1rem">
        {stat.instances > 0 && `· ${instancesText(stat.instances)}`}
      </Text>
    </Box>
  );
};
