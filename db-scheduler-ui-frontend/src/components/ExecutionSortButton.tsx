import { Text } from '@chakra-ui/react';
import React from 'react';
import { ChevronDownIcon } from '@chakra-ui/icons';
import { SortBy } from 'src/services/getTasks';

export const ExecutionSortButton: React.FC<{
  currentSort: SortBy;
  setCurrentSort: (sorting: SortBy) => void;
}> = ({ currentSort, setCurrentSort }) => {
  return (
    <Text
      as="button"
      flex="2"
      textAlign="left"
      fontSize={'sm'}
      _active={{ color: '#000000', fontWeight: 'semibold' }}
      onClick={() => setCurrentSort(SortBy.Default)}
      fontWeight={currentSort === SortBy.Default ? 'bold' : 'normal'}
    >
      Next Execution Time <ChevronDownIcon fontSize={'xl'} />
    </Text>
  );
};
