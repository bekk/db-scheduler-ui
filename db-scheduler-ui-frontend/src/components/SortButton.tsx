import { Text } from '@chakra-ui/react';
import React from 'react';
import { ChevronDownIcon } from '@chakra-ui/icons';
import { SortBy } from 'src/services/getTasks';

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
    //_active={{ color: '#000000', fontWeight: 'semibold' }}
    onClick={() =>
      currentSort === name ? setSortAsc(!sortAsc) : setCurrentSort(name)
    }
    //fontWeight={currentSort === name ? 'bold' : 'normal'}
  >
    {title}
    <ChevronDownIcon
      fontSize={'xl'}
      transform={
        sortAsc || currentSort !== name ? 'rotate(0deg)' : 'rotate(180deg)'
      }
      color={currentSort === name ? '#000000' : '#ababab'}
      transition="transform 0.3s ease-in-out"
    />
  </Text>
);
