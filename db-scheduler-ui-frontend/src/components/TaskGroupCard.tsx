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
import React, { useRef, useState, useEffect } from 'react';
import { Task } from '../models/Task';
import TaskCard from './TaskCard';
import { Box } from '@chakra-ui/react';

interface TaskCardProps extends Task {
  refetch: () => void;
}

const TaskGroupCard: React.FC<TaskCardProps> = (taskProps) => {
  const [marginBottom, setMarginBottom] = useState(0);
  const ref = useRef<HTMLDivElement>(null);

  const updateRef = () => {
    if (ref.current) {
      const el = ref.current;
      setMarginBottom(-(el.clientHeight * 1.8));
    }
  };

  useEffect(() => {
    updateRef();
    window.addEventListener('resize', updateRef); // updateRef on resize to avoid overlapping
    return () => {
      window.removeEventListener('resize', updateRef);
    };
  }, []);

  return (
    <Box mb={`${marginBottom}px`}>
      <Box zIndex={0} mr={2} pos={'relative'}>
        <TaskCard
          {...taskProps}
          accordionProps={{
            shadow: 'md',
          }}
        />
      </Box>
      <Box
        pos={'relative'}
        left="0"
        zIndex={-3}
        mx={1}
        transform={'translateY(-90%)'}
      >
        <TaskCard
          {...taskProps}
          accordionProps={{
            shadow: 'md',
            mt: -1,
          }}
        />
      </Box>
      <Box
        ref={ref}
        pos={'relative'}
        left="0"
        zIndex={-4}
        ml={2}
        transform={'translateY(-180%)'}
      >
        <TaskCard
          {...taskProps}
          accordionProps={{
            shadow: 'md',
            mt: -1,
          }}
        />
      </Box>
    </Box>
  );
};

export default TaskGroupCard;
