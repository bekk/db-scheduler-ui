import React from 'react';
import { Box, IconButton, Input, Text } from '@chakra-ui/react';
import { FilterBy } from 'src/services/getTasks';
import { FilterBox } from './FilterBox';
import colors from 'src/styles/colors';
import { RepeatIcon } from '@chakra-ui/icons';

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
        <Text>Refresh</Text>
        <IconButton // TODO: add refresh functionality
          borderColor={colors.primary['300']}
          borderWidth={1}
          textAlign={'left'}
          fontWeight={'normal'}
          bgColor={colors.primary[100]}
          aria-label={'refresh tasks'}
          icon={<RepeatIcon boxSize={7} />}
          ml={2}
          boxSize={45}
        />
      </Box>
    </Box>
  </Box>
);
