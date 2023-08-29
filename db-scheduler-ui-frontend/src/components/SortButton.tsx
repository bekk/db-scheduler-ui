import { Text } from '@chakra-ui/react';
import React from 'react';
import { ChevronDownIcon } from '@chakra-ui/icons';
import { SortBy } from 'src/services/getTasks';

export const SortButton: React.FC<{
  title: string;
  name: SortBy;
  currentSort: SortBy;
  setCurrentSort: (sorting: SortBy) => void;
}> = ({ title, currentSort, setCurrentSort, name }) => {
  return (
    <Text
      as="button"
      flex="2"
      textAlign="left"
      fontSize={'sm'}
      _active={{ color: '#000000', fontWeight: 'semibold' }}
      onClick={() => setCurrentSort(name)}
      fontWeight={currentSort === name ? 'bold' : 'normal'}
    >
      {title}
      <ChevronDownIcon fontSize={'xl'} />
    </Text>
  );
};
