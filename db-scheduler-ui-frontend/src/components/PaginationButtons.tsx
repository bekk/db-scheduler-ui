import { Box, Button } from '@chakra-ui/react';
import { ChevronLeftIcon, ChevronRightIcon } from '@chakra-ui/icons';
import { DoubleChevronIcon } from 'src/assets/icons/DoubleChevronIcon';
import { PaginationParams } from 'src/services/getTasks';
import React from 'react';

interface PaginationButtonsProps {
  page: PaginationParams;
  setPage: (value: React.SetStateAction<PaginationParams>) => void;
  numberOfPages: number | undefined;
}

const PaginationButtons: React.FC<PaginationButtonsProps> = ({
  page,
  setPage,
  numberOfPages,
}) => (
  <Box float={'right'} display={'flex'} alignItems={'center'}>
    {page.pageNumber > 1 && (
      <Button
        variant={'unstyled'}
        onClick={() =>
          setPage((prev) => {
            return { ...prev, pageNumber: 0 };
          })
        }
        visibility={page.pageNumber > 1 ? 'visible' : 'hidden'}
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
    {page.pageNumber > 0 && (
      <Button
        variant={'unstyled'}
        onClick={() =>
          setPage((prev) => {
            return { ...prev, pageNumber: page.pageNumber - 1 };
          })
        }
      >
        <ChevronLeftIcon fontSize={24} m={0} />
      </Button>
    )}
    {'page ' + (page.pageNumber + 1) + ' of ' + (numberOfPages || 1)}
    <Button
      variant={'unstyled'}
      onClick={() =>
        setPage((prev) => {
          return { ...prev, pageNumber: page.pageNumber + 1 };
        })
      }
      visibility={
        page.pageNumber + 1 < (numberOfPages || 0) ? 'visible' : 'hidden'
      }
      p={0}
      m={0}
    >
      <ChevronRightIcon fontSize={24} m={0} />
    </Button>
    <Button
      variant={'unstyled'}
      onClick={() =>
        setPage((prev) => {
          return { ...prev, pageNumber: numberOfPages ? numberOfPages - 1 : 0 };
        })
      }
      visibility={
        page.pageNumber + 2 < (numberOfPages || 0) ? 'visible' : 'hidden'
      }
      p={0}
      m={0}
    >
      <DoubleChevronIcon fontSize={24} m={0} />
    </Button>
  </Box>
);

export default PaginationButtons;
