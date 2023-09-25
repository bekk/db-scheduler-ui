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
import { Text } from '@chakra-ui/react';
import React from 'react';
import { ChevronDownIcon } from '@chakra-ui/icons';
import colors from 'src/styles/colors';
import { SortBy } from 'src/models/QueryParams';

export const SortButton: React.FC<{
  title: string;
  name: SortBy;
  currentSort: SortBy;
  setCurrentSort: (sorting: SortBy) => void;
  sortAsc: boolean;
  setSortAsc: (sortAsc: boolean) => void;
}> = ({ title, currentSort, setCurrentSort, name, sortAsc, setSortAsc }) => (
  <Text
    as="button"
    flex="2"
    textAlign="left"
    fontSize={'sm'}
    textColor={colors.primary['500']}
    onClick={() =>
      currentSort === name ? setSortAsc(!sortAsc) : setCurrentSort(name)
    }
  >
    {title}
    <ChevronDownIcon
      fontSize={'xl'}
      transform={
        sortAsc || currentSort !== name ? 'rotate(0deg)' : 'rotate(180deg)'
      }
      color={
        currentSort === name ? colors.primary['500'] : colors.primary['300']
      }
      transition="transform 0.3s ease-in-out"
    />
  </Text>
);
