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
  Box,
  Button,
  Menu,
  MenuButton,
  MenuItem,
  MenuList,
} from '@chakra-ui/react';
import React from 'react';
import { ChevronDownIcon } from '@chakra-ui/icons';
import colors from 'src/styles/colors';
import { FilterBy } from 'src/models/QueryParams';

export const FilterBox: React.FC<{
  currentFilter: FilterBy;
  setCurrentFilter: (filter: FilterBy) => void;
  history?: boolean;
}> = ({ currentFilter, setCurrentFilter, history }) => {
  const filters = history
    ? [FilterBy.All, FilterBy.Failed, FilterBy.Succeeded]
    : [FilterBy.All, FilterBy.Failed, FilterBy.Running, FilterBy.Scheduled];

  return (
    <Box
      display={'flex'}
      mb={2}
      mt={2}
      flex={1}
      justifyContent={'end'}
      alignSelf={'start'}
    >
      <Menu>
        <MenuButton
          as={Button}
          rightIcon={<ChevronDownIcon />}
          backgroundColor={colors.primary['100']}
          borderColor={colors.primary['300']}
          borderWidth={1}
          width={200}
          textAlign={'left'}
          fontWeight={'normal'}
          ml={14}
        >
          Status: {currentFilter}
        </MenuButton>
        <MenuList p={0}>
          {filters.map((filterValue) => (
            <MenuItem
              key={filterValue}
              onClick={() => setCurrentFilter(filterValue)}
              fontWeight={currentFilter === filterValue ? 'bold' : 'normal'}
            >
              {filterValue}
            </MenuItem>
          ))}
        </MenuList>
      </Menu>
    </Box>
  );
};
