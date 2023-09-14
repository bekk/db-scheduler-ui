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
}> = ({ currentFilter, setCurrentFilter }) => (
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
        <MenuItem
          pt={2}
          borderRadius={'3px 3px 0 0'}
          onClick={() => setCurrentFilter(FilterBy.All)}
          fontWeight={currentFilter === FilterBy.All ? 'bold' : 'normal'}
        >
          All
        </MenuItem>
        <MenuItem
          onClick={() => setCurrentFilter(FilterBy.Failed)}
          fontWeight={currentFilter === FilterBy.Failed ? 'bold' : 'normal'}
        >
          Failed
        </MenuItem>
        <MenuItem
          onClick={() => setCurrentFilter(FilterBy.Running)}
          fontWeight={currentFilter === FilterBy.Running ? 'bold' : 'normal'}
        >
          Running
        </MenuItem>
        <MenuItem
          pb={2}
          borderRadius={'0 0 3px 3px'}
          onClick={() => setCurrentFilter(FilterBy.Scheduled)}
          fontWeight={currentFilter === FilterBy.Scheduled ? 'bold' : 'normal'}
        >
          Scheduled
        </MenuItem>
      </MenuList>
    </Menu>
  </Box>
);
