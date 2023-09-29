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
  Box,
  Button,
  IconButton,
  Menu,
  MenuButton,
  MenuItem,
  MenuList,
} from '@chakra-ui/react';
import deleteTask from 'src/services/deleteTask';
import React from 'react';
import { CalendarIcon, DeleteIcon, InfoOutlineIcon } from '@chakra-ui/icons';
import { IoEllipsisVerticalIcon } from '../../assets/icons';
import { useNavigate } from 'react-router-dom';
import { ScheduleRunAlert } from './ScheduleRunAlert';

interface TaskProps {
  taskName: string;
  taskInstance: string;
  style?: React.CSSProperties;
  refetch?: () => void;
}

export const DotButton: React.FC<TaskProps> = ({
  taskName,
  taskInstance,
  style,
  refetch,
}) => {
  const [deleteOpen, setDeleteOpen] = React.useState(false);
  const [scheduleOpen, setScheduleOpen] = React.useState(false);
  const navigate = useNavigate();
  const onClose = () => setDeleteOpen(false);
  const cancelRef = React.useRef(null);
  return (
    <Box style={style}>
      <Menu>
        <MenuButton
          as={IconButton}
          rounded={6}
          aria-label="Options"
          variant="outline"
          icon={<IoEllipsisVerticalIcon />}
          onClick={(event) => event.stopPropagation()}
        />

        <MenuList padding={0}>
          <MenuItem
            rounded={6}
            minBlockSize={10}
            onClick={(event) => {
              event.stopPropagation();
              setScheduleOpen(true);
            }}
            icon={<CalendarIcon boxSize={4} />}
          >
            Update execution time
          </MenuItem>
          <MenuItem
            rounded={6}
            minBlockSize={10}
            onClick={(event) => {
              navigate(`/history/all`, { state: { taskName, taskInstance } });
              event.stopPropagation();
            }}
            icon={<InfoOutlineIcon boxSize={4} />}
          >
            See history for task
          </MenuItem>
          <MenuItem
            rounded={6}
            minBlockSize={10}
            onClick={(event) => {
              event.stopPropagation();
              setDeleteOpen(true);
            }}
            icon={<DeleteIcon boxSize={4} />}
          >
            Delete task
          </MenuItem>
        </MenuList>
      </Menu>
      <ScheduleRunAlert
        failed={true}
        isOpen={scheduleOpen}
        setIsopen={setScheduleOpen}
        taskId={taskInstance}
        taskName={taskName}
        refetch={refetch ?? (() => {})}
      />
      <AlertDialog
        isOpen={deleteOpen}
        leastDestructiveRef={cancelRef}
        onClose={onClose}
      >
        <AlertDialogOverlay>
          <AlertDialogContent>
            <AlertDialogHeader fontSize="lg" fontWeight="bold">
              Delete
            </AlertDialogHeader>

            <AlertDialogBody>
              Are you sure you want to delete, {taskName} Task-ID:{taskInstance}
            </AlertDialogBody>

            <AlertDialogFooter>
              <Button
                ref={cancelRef}
                onClick={(event) => {
                  event.stopPropagation();
                  onClose();
                }}
              >
                Cancel
              </Button>
              <Button
                colorScheme="red"
                onClick={(event) => {
                  event.stopPropagation();
                  deleteTask(taskInstance, taskName);
                  onClose();
                }}
                ml={3}
              >
                Delete
              </Button>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialogOverlay>
      </AlertDialog>
    </Box>
  );
};
