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
import {
  AlertDialog,
  AlertDialogBody,
  AlertDialogContent,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogOverlay,
  Button,
} from '@chakra-ui/react';
import React from 'react';
import runTaskGroup from 'src/services/runTaskGroup';
import colors from 'src/styles/colors';

interface TaskProps {
  onlyFailed: boolean;
  taskName: string;
  style?: React.CSSProperties;
  isOpen: boolean;
  setIsopen: React.Dispatch<React.SetStateAction<string>>;
  runFunction: (name: string, onlyFailed: boolean) => void;
}

export const RunAllAlert: React.FC<TaskProps> = ({
  onlyFailed,
  taskName,
  isOpen,
  setIsopen,
}) => {
  const cancelRef = React.useRef(null);
  return (
    <AlertDialog
      isOpen={isOpen}
      leastDestructiveRef={cancelRef}
      onClose={() => setIsopen('')}
    >
      <AlertDialogOverlay>
        <AlertDialogContent>
          <AlertDialogHeader fontSize="lg" fontWeight="bold">
            {onlyFailed ? 'Rerun All Failed' : 'Run All'}
          </AlertDialogHeader>

          <AlertDialogBody>
            Are you sure you want to{' '}
            {onlyFailed ? 'rerun all failed' : 'run all'} tasks with taskname:{' '}
            {taskName}. This will include the ones outside of your current
            filters and search.
          </AlertDialogBody>

          <AlertDialogFooter>
            <Button
              onClick={() => {
                setIsopen('');
              }}
            >
              Cancel
            </Button>
            <Button
              bgColor={onlyFailed ? colors.running[300] : colors.running[100]}
              _hover={{
                backgroundColor: onlyFailed
                  ? colors.running[200]
                  : colors.running[100],
              }}
              _active={{
                backgroundColor: onlyFailed
                  ? colors.running[200]
                  : colors.running[300],
              }}
              textColor={onlyFailed ? colors.primary[100] : colors.running[500]}
              onClick={() => {
                runTaskGroup(taskName, onlyFailed);
              }}
              ml={3}
            >
              {onlyFailed ? 'Rerun All' : 'Run All'}
            </Button>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialogOverlay>
    </AlertDialog>
  );
};
