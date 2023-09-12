import { Box, Button } from '@chakra-ui/react';
import { ChevronLeftIcon, ChevronRightIcon } from '@chakra-ui/icons';
import { DoubleChevronIcon } from 'src/assets/icons/DoubleChevronIcon';
import React from 'react';

interface PaginationButtonsProps {
  page: number;
  limit: number;
  setPage: (value: number) => void;
  numberOfPages: number | undefined;
}

const PaginationButtons: React.FC<PaginationButtonsProps> = ({
  page,
  setPage,
  numberOfPages,
}) => (
  <Box float={'right'} display={'flex'} alignItems={'center'}>
    {page > 2 && (
      <Button
        variant={'unstyled'}
        onClick={() => setPage(1)}
        visibility={page > 1 ? 'visible' : 'hidden'}
        p={0}
        m={0}
      >
        <DoubleChevronIcon
          direction={180}
          fontSize={24}
          m={0}
          transform={'scaleX(-1)'}
        />
      </Button>
    )}
    {page > 1 && (
      <Button variant={'unstyled'} onClick={() => setPage(page - 1)}>
        <ChevronLeftIcon fontSize={24} m={0} />
      </Button>
    )}
    {'page ' + page + ' of ' + (numberOfPages || 1)}
    <Button
      variant={'unstyled'}
      onClick={() => setPage(page + 1)}
      visibility={page < (numberOfPages || 1) ? 'visible' : 'hidden'}
      p={0}
      m={0}
    >
      <ChevronRightIcon fontSize={24} m={0} />
    </Button>
    <Button
      variant={'unstyled'}
      onClick={() => setPage(numberOfPages ? numberOfPages : 1)}
      visibility={page + 1 < (numberOfPages || 1) ? 'visible' : 'hidden'}
      p={0}
      m={0}
    >
      <DoubleChevronIcon fontSize={24} m={0} />
    </Button>
  </Box>
);

export default PaginationButtons;
