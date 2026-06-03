/*
 * Copyright (C) Bekk
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { Box } from '@chakra-ui/react';
import { Route, Routes } from 'react-router-dom';
import { TopBar } from 'src/components/common/TopBar';
import { LogList } from 'src/components/history/LogList';
import TaskList from 'src/components/scheduled/TaskList';
import { getShowHistory } from 'src/utils/config';

export const FrontPage: React.FC = () => {
  const showHistory = getShowHistory();
  return (
    <>
      <TopBar title={'DB Scheduler UI'} />
      <Box mx={20} mt={14}>
        <Routes>
          <Route index element={<TaskList />}></Route>
          <Route path="/:taskName" element={<TaskList />}></Route>
          <Route path="/:taskName/page/:page" element={<TaskList />}></Route>
          <Route
            path="/history/:taskName/:taskInstance"
            element={<LogList />}
          ></Route>
          {showHistory && (
            <Route path="/history/all" element={<LogList />}></Route>
          )}
        </Routes>
      </Box>
    </>
  );
};
