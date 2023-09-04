import React from 'react';

import { Box, HStack } from '@chakra-ui/react';
import { SortBy } from 'src/services/getTasks';

import { SortButton } from 'src/components/SortButton';

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
}) => {
  console.log(isDetailsView);
  return (
    <HStack
      display={'flex'}
      p="8px 16px"
      justifyContent={'space-around'}
      spacing={5}
    >
      <Box flex="1" textAlign="left" textColor={'#484848'} fontSize={'sm'}>
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
      <Box flex="2" textAlign="left" textColor={'#484848'} fontSize={'sm'}>
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
};

export default TitleRow;
