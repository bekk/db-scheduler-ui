import React from 'react';
import { Box, Input, Text } from '@chakra-ui/react';
import { FilterBy } from 'src/services/getTasks';
import { FilterBox } from './FilterBox';
import { RefreshButton } from './RefreshButton';

interface HeaderBarProps {
  inputPlaceholder: string;
  taskName: string;
  currentFilter: FilterBy;
  setCurrentFilter: (filter: FilterBy) => void;
  setSearchTerm: (searchTerm: string) => void;
  title: string;
  history?: boolean;
}

export const HeaderBar: React.FC<HeaderBarProps> = ({
  inputPlaceholder,
  currentFilter,
  setCurrentFilter,
  setSearchTerm,
  refetch,
  title,
  history,
}) => (
  <Box
    display={'flex'}
    mb={7}
    alignItems={'center'}
    justifyContent={'space-between'}
    w={'100%'}
  >
    <Box display={'flex'} alignItems={'center'} flex={1}>
      <Box>
        <Text ml={1} fontSize={'3xl'} fontWeight={'semibold'}>
          {title}
        </Text>
        <Input
          placeholder={inputPlaceholder}
          onChange={(e) => setSearchTerm(e.currentTarget.value)}
          bgColor={'white'}
          w={'30vmax'}
          mt={7}
          ml={1}
        />
      </Box>
    </Box>
    <Box height={'100%'}>
      <FilterBox
        currentFilter={currentFilter}
        setCurrentFilter={setCurrentFilter}
        history={history}
      />
      <Box display={'flex'} float={'right'} alignItems={'center'}>
        <RefreshButton s="sad" />
      </Box>
    </Box>
  </Box>
);
