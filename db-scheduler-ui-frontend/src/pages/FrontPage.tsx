import TaskList from 'src/components/TaskList';
import { TopBar } from 'src/components/TopBar';
import { Box } from '@chakra-ui/react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';

export const FrontPage: React.FC = () => {
  return (
    <>
      <TopBar title={'JobJuggler'} />
      <Box mx={20} mt={14}>
        <Router basename="/db-scheduler-ui">
          <Routes>
            <Route index path="/" element={<TaskList />}></Route>
            <Route path="/:taskName" element={<TaskList />}></Route>
          </Routes>
        </Router>
      </Box>
    </>
  );
};
