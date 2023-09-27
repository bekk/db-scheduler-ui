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
import React from 'react';

import { Box, HStack } from '@chakra-ui/react';

import { SortButton } from 'src/components/input/SortButton';
import colors from 'src/styles/colors';
import { SortBy } from 'src/models/QueryParams';

interface TitleRowProps {
  currentSort: SortBy;
  setCurrentSort: React.Dispatch<React.SetStateAction<SortBy>>;
  sortAsc: boolean;
  setSortAsc: React.Dispatch<React.SetStateAction<boolean>>;
  isDetailsView?: boolean;
}

const TitleRow: React.FC<TitleRowProps> = ({
  currentSort,
  setCurrentSort,
  setSortAsc,
  sortAsc,
  isDetailsView,
}) => (
  <HStack
    display={'flex'}
    p="8px 16px"
    justifyContent={'space-around'}
    spacing={5}
  >
    <Box
      flex="1"
      textAlign="left"
      textColor={colors.primary['600']}
      fontSize={'sm'}
    >
      Status
    </Box>
    {!isDetailsView && (
      <SortButton
        currentSort={currentSort}
        setCurrentSort={setCurrentSort}
        sortAsc={sortAsc}
        setSortAsc={setSortAsc}
        title={'Task Name'}
        name={SortBy.Name}
      />
    )}
    <Box
      flex="2"
      textAlign="left"
      textColor={colors.primary['600']}
      fontSize={'sm'}
    >
      Task-ID
    </Box>
    <SortButton
      currentSort={currentSort}
      setCurrentSort={setCurrentSort}
      sortAsc={sortAsc}
      setSortAsc={setSortAsc}
      title={'Next Execution Time'}
      name={SortBy.Default}
    />
  </HStack>
);

export default TitleRow;
