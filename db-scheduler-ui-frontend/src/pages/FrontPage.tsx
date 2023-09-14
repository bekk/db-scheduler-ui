import { TopBar } from 'src/components/TopBar';
import { Box } from '@chakra-ui/react';
import { Route, Routes } from 'react-router-dom';
import TaskList from 'src/components/TaskList';
import { LogList } from 'src/components/history/LogList';

export const FrontPage: React.FC = () => {
  return (
    <>
      <TopBar title={'DB Scheduler UI'} />
      <Box mx={20} mt={14}>
        <Routes>
          <Route index element={<TaskList />}></Route>
          <Route path="/:taskName" element={<TaskList />}></Route>
          <Route path="/:taskName/page/:page" element={<TaskList />}></Route>
          <Route path="/:taskName/:taskInstance" element={<LogList />}></Route>
          <Route path="/all" element={<LogList />}></Route>
        </Routes>
      </Box>
    </>
  );
};
