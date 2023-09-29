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
import React from 'react';

import JsonViewer from 'src/components/common/JsonViewer';

export const LogDataRow: React.FC<{ taskData: object | null }> = ({
  taskData,
}) => {
  return (
    <Box ml={'16px'}>
      {taskData !== null && (
        <Box display={'flex'} flexDirection={'column'}>
          <JsonViewer data={taskData} />
        </Box>
      )}
    </Box>
  );
};
