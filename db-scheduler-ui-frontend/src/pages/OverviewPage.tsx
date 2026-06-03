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
import { Box, Heading, Spinner, Text } from '@chakra-ui/react';
import React from 'react';
import { OverviewTable } from 'src/components/overview/OverviewTable';
import { useOverview } from 'src/hooks/useOverview';
import colors from 'src/styles/colors';

export const OverviewPage: React.FC = () => {
  const { data, isLoading, isError } = useOverview();

  return (
    <Box>
      <Heading as="h1" size="lg" mb={6} color={colors.primary['900']}>
        All tasks
      </Heading>

      {isLoading && <Spinner aria-label="Loading overview" />}

      {isError && (
        <Text color={colors.failed['200']}>Could not load the task overview.</Text>
      )}

      {data &&
        (data.length === 0 ? (
          <Text color={colors.primary['400']}>No tasks found.</Text>
        ) : (
          <OverviewTable tasks={data} />
        ))}

      <Text mt={8} fontSize="sm" color={colors.primary['400']}>
        Navigation-only page — no Run / Rerun / Delete. Hover Next/Last run for
        absolute timestamps.
      </Text>
    </Box>
  );
};
