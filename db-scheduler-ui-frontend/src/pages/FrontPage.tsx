import TaskList from 'src/components/TaskList';
import { TopBar } from 'src/components/TopBar';
import { Box } from '@chakra-ui/react';

export const FrontPage: React.FC = () => {
  return (
    <>
      <TopBar title={'JobJuggler'} />
      <Box mx={20} mt={14}>
        <TaskList />
      </Box>
    </>
  );
};
