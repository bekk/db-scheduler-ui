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
import { Box, Text } from '@chakra-ui/react';
import React from 'react';
import { OverviewRow } from 'src/components/overview/OverviewRow';
import { OverviewTask } from 'src/models/Overview';
import colors from 'src/styles/colors';

interface OverviewSectionProps {
  title: string;
  tasks: OverviewTask[];
  now: number;
}

export const OverviewSection: React.FC<OverviewSectionProps> = ({
  title,
  tasks,
  now,
}) => {
  if (tasks.length === 0) return null;

  return (
    <Box as="section" mb={8}>
      <Text
        as="h2"
        fontSize="xs"
        fontWeight="bold"
        letterSpacing="wide"
        color={colors.primary['400']}
        mb={3}
      >
        {title} · {tasks.length}
      </Text>
      {tasks.map((task) => (
        <OverviewRow key={task.taskName} task={task} now={now} />
      ))}
    </Box>
  );
};
