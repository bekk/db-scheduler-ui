import { Text } from '@chakra-ui/react';
import React from 'react';
import { ChevronDownIcon } from '@chakra-ui/icons';
import { SortBy } from 'src/services/getTasks';

export const TaskNameSortButton: React.FC<{
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
      onClick={() => setCurrentSort(SortBy.Name)}
      fontWeight={currentSort === SortBy.Name ? 'bold' : 'normal'}
    >
      Task Name <ChevronDownIcon fontSize={'xl'} />
    </Text>
  );
};
