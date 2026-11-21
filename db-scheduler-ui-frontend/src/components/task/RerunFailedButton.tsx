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
  useToast,
} from '@chakra-ui/react';
import { useRef, useState } from 'react';
import { rerunFailed } from 'src/services/rerunFailed';
import { getReadonly } from 'src/utils/config';

/** Reruns every failing execution of the task, not only the loaded ones, after a confirmation. */
export const RerunFailedButton: React.FC<{
  taskName: string;
  failing: number;
  onDone: () => void;
}> = ({ taskName, failing, onDone }) => {
  const [confirming, setConfirming] = useState(false);
  const [busy, setBusy] = useState(false);
  const cancelRef = useRef<HTMLButtonElement>(null);
  const toast = useToast();

  if (getReadonly() || failing === 0) {
    return null;
  }

  const run = async () => {
    setConfirming(false);
    setBusy(true);
    try {
      const { rescheduled } = await rerunFailed(taskName, new Date());
      toast({
        title: `Rescheduled ${rescheduled} instance${
          rescheduled === 1 ? '' : 's'
        }`,
        status: 'success',
        duration: 3000,
      });
      onDone();
    } catch (error) {
      toast({
        title: error instanceof Error ? error.message : 'Rerun failed',
        status: 'error',
        duration: 5000,
      });
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <Button
        size="sm"
        colorScheme="red"
        variant="outline"
        isDisabled={busy}
        onClick={() => setConfirming(true)}
      >
        Rerun all {failing.toLocaleString()} failed…
      </Button>
      <AlertDialog
        isOpen={confirming}
        leastDestructiveRef={cancelRef}
        onClose={() => setConfirming(false)}
      >
        <AlertDialogOverlay>
          <AlertDialogContent>
            <AlertDialogHeader fontSize="lg" fontWeight="bold">
              Rerun failed instances
            </AlertDialogHeader>
            <AlertDialogBody>
              Rerun all {failing.toLocaleString()} failing instances of{' '}
              {taskName}, not only the ones loaded. This resets their failure
              counts and last-failure times.
            </AlertDialogBody>
            <AlertDialogFooter>
              <Button ref={cancelRef} onClick={() => setConfirming(false)}>
                Cancel
              </Button>
              <Button colorScheme="red" ml={3} onClick={() => void run()}>
                Rerun
              </Button>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialogOverlay>
      </AlertDialog>
    </>
  );
};
