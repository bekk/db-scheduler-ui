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
import colors from 'src/styles/colors';
import { DateTimeInput } from './DateTimeInput';

interface TaskProps {
  failed: boolean;
  taskName: string;
  isOpen: boolean;
  setIsopen: React.Dispatch<React.SetStateAction<boolean>>;
}

export const ScheduleRunAlert: React.FC<TaskProps> = ({
  failed,
  isOpen,
  setIsopen,
}) => {
  const cancelRef = React.useRef(null);
  const [time, setTime] = React.useState<Date | null>(new Date());

  return (
    <AlertDialog
      isOpen={isOpen}
      leastDestructiveRef={cancelRef}
      onClose={() => setIsopen(false)}
    >
      <AlertDialogOverlay>
        <AlertDialogContent>
          <AlertDialogHeader fontSize="lg" fontWeight="bold">
            {failed ? 'Rerun Task At Time' : 'Run Task At Time'}
          </AlertDialogHeader>

          <AlertDialogBody>
            <DateTimeInput onChange={(e) => setTime(e)} selectedDate={time} />
          </AlertDialogBody>

          <AlertDialogFooter>
            <Button
              onClick={() => {
                setIsopen(false);
              }}
            >
              Cancel
            </Button>
            <Button
              bgColor={failed ? colors.running[300] : colors.running[100]}
              _hover={{
                backgroundColor: failed
                  ? colors.running[200]
                  : colors.running[100],
              }}
              _active={{
                backgroundColor: failed
                  ? colors.running[200]
                  : colors.running[300],
              }}
              textColor={failed ? colors.primary[100] : colors.running[500]}
              onClick={() => {
                setIsopen(false);
              }}
              ml={3}
            >
              {failed ? 'Rerun All' : 'Run All'}
            </Button>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialogOverlay>
    </AlertDialog>
  );
};
