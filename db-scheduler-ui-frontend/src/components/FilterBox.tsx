import {
  Box,
  Button,
  Menu,
  MenuButton,
  MenuItem,
  MenuList,
} from '@chakra-ui/react';
import { FilterBy } from 'src/services/getTasks';
import React from 'react';
import { ChevronDownIcon } from '@chakra-ui/icons';
import colors from 'src/styles/colors';

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
