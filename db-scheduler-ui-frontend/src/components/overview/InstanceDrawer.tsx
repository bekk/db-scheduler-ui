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
  Drawer,
  DrawerBody,
  DrawerCloseButton,
  DrawerContent,
  DrawerHeader,
  DrawerOverlay,
  HStack,
  Link,
  Text,
  useToast,
} from '@chakra-ui/react';
import { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import JsonViewer from 'src/components/common/JsonViewer';
import { useInstanceDetail } from 'src/hooks/useInstanceDetail';
import {
  InstanceDetail,
  InstanceRun,
  InstanceStatus,
} from 'src/models/InstanceDetail';
import { isOverdue, overdueColor } from 'src/utils/overdue';
import deleteTask from 'src/services/deleteTask';
import runTask from 'src/services/runTask';
import colors from 'src/styles/colors';
import { dateFormatText } from 'src/utils/dateFormatText';
import { getReadonly } from 'src/utils/config';
import { formatDistanceToNowStrict } from 'date-fns';

interface InstanceDrawerProps {
  /** The task whose sole execution to show; null keeps the drawer closed. */
  taskName: string | null;
  onClose: () => void;
  /** Refresh the overview behind the drawer after an action changes the execution. */
  onChanged: () => void;
}

const statusLabel: Record<InstanceStatus, string> = {
  FAILED: 'Failed',
  RUNNING: 'Running',
  SCHEDULED: 'Scheduled',
};

const statusColor: Record<InstanceStatus, string> = {
  FAILED: colors.failed['200'],
  RUNNING: colors.running['300'],
  SCHEDULED: colors.primary['500'],
};

/** Slide-over detail for a single execution. See `specs/instance-panel/spec.md`. */
export const InstanceDrawer: React.FC<InstanceDrawerProps> = ({
  taskName,
  onClose,
  onChanged,
}) => {
  const { instance, status, refetch } = useInstanceDetail(taskName);
  // react-query keeps `data` from the last successful fetch around on a failed poll, so gate on
  // `status` rather than `instance` — otherwise the header keeps a stale badge after the body
  // has already moved on to a gone/ambiguous/error message.
  const showInstance = status === 'ready' && !!instance;

  return (
    <Drawer isOpen={!!taskName} placement="right" onClose={onClose} size="md">
      <DrawerOverlay />
      <DrawerContent>
        <DrawerCloseButton />
        <DrawerHeader borderBottomWidth="1px" pb={3}>
          <HStack spacing={3} align="center">
            <Text fontSize="lg" color={colors.primary['600']}>
              {showInstance ? instance.id : taskName}
            </Text>
            {showInstance && (
              <Text
                fontSize="sm"
                fontWeight="bold"
                color={statusColor[instance.status]}
              >
                {statusLabel[instance.status]}
              </Text>
            )}
          </HStack>
          {/* Only once loaded: before that the title above already shows the task name. */}
          {showInstance && (
            <Text
              fontSize="xs"
              fontWeight="normal"
              color={colors.primary['400']}
            >
              {taskName}
            </Text>
          )}
        </DrawerHeader>

        <DrawerBody>
          {status === 'loading' && <Muted>Loading instance…</Muted>}
          {status === 'error' && <Muted>Could not load this instance.</Muted>}
          {status === 'gone' && (
            <Muted>
              This execution is no longer scheduled — it may have completed or
              been deleted.
            </Muted>
          )}
          {status === 'ambiguous' && (
            <Muted>
              This task has more than one execution now, so there is no single
              one to show.
            </Muted>
          )}
          {status === 'ready' && instance && (
            <>
              <Facts instance={instance} />
              <Section label="Task data" />
              {instance.taskData ? (
                <Box
                  bgColor={colors.primary['200']}
                  borderRadius="6px"
                  p={3}
                  fontSize="sm"
                  overflowX="auto"
                >
                  <JsonViewer data={instance.taskData} />
                </Box>
              ) : (
                <Muted>no task data</Muted>
              )}

              <ExceptionSection instance={instance} />
              <HistorySection instance={instance} />
              <Actions
                // Remounts on a different instance so confirm/busy state doesn't survive a
                // switch (e.g. Back navigating from one instance's delete-confirm to another's).
                key={`${instance.taskName}:${instance.id}`}
                instance={instance}
                onDone={() => {
                  refetch();
                  onChanged();
                }}
                onDeleted={() => {
                  onChanged();
                  onClose();
                }}
              />
            </>
          )}
        </DrawerBody>
      </DrawerContent>
    </Drawer>
  );
};

const Muted: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <Text color={colors.primary['400']} fontSize="sm">
    {children}
  </Text>
);

const Section: React.FC<{ label: string; gated?: boolean }> = ({
  label,
  gated,
}) => (
  <HStack spacing={2} mt={6} mb={2}>
    <Text
      fontSize="xs"
      fontWeight="bold"
      textTransform="uppercase"
      letterSpacing="wider"
      color={colors.primary['400']}
    >
      {label}
    </Text>
    {gated && (
      <Text
        fontSize="10px"
        bgColor={colors.primary['200']}
        color={colors.primary['400']}
        borderRadius="4px"
        px={1}
      >
        history
      </Text>
    )}
  </HStack>
);

const Fact: React.FC<{
  label: string;
  value: React.ReactNode;
  color?: string;
  bold?: boolean;
}> = ({ label, value, color, bold }) => (
  <HStack
    justify="space-between"
    align="baseline"
    py={2}
    borderBottom="1px solid"
    borderBottomColor={colors.primary['200']}
    fontSize="sm"
  >
    <Text color={colors.primary['500']}>{label}</Text>
    <Text
      color={color ?? colors.primary['600']}
      fontWeight={bold ? 'bold' : 'normal'}
    >
      {value}
    </Text>
  </HStack>
);

const Facts: React.FC<{ instance: InstanceDetail }> = ({ instance }) => (
  <Box>
    <Fact
      label="Consecutive failures"
      value={instance.consecutiveFailures}
      color={
        instance.consecutiveFailures > 0 ? colors.failed['200'] : undefined
      }
      bold={instance.consecutiveFailures > 0}
    />
    <Fact
      label="Next execution"
      value={
        instance.executionTime
          ? `${dateFormatText(new Date(instance.executionTime))}${
              isOverdue(instance.executionTime, instance.picked)
                ? ' · overdue'
                : ''
            }`
          : never()
      }
      color={
        isOverdue(instance.executionTime, instance.picked)
          ? overdueColor
          : undefined
      }
      bold={isOverdue(instance.executionTime, instance.picked)}
    />
    <Fact label="Last failure" value={timestamp(instance.lastFailure)} />
    <Fact label="Last success" value={timestamp(instance.lastSuccess)} />
    <Fact
      label="Picked by"
      value={instance.picked ? instance.pickedBy : notRunning()}
    />
  </Box>
);

// An absent history block means db-scheduler-ui.history is off.
const ExceptionSection: React.FC<{ instance: InstanceDetail }> = ({
  instance,
}) => {
  if (!instance.history) {
    return (
      <>
        <Section label="Last exception" />
        <Muted>
          Stack traces come from the log table. Enable{' '}
          <code>db-scheduler-ui.history</code> to see the exception and this
          instance&apos;s run history — the failure count and timestamp above
          are available either way.
        </Muted>
      </>
    );
  }

  const failure = instance.history.lastFailure;
  return (
    <>
      <Section label="Last exception" gated />
      {failure ? (
        <Box>
          <Text fontSize="sm" color={colors.failed['200']} fontWeight="bold">
            {failure.exceptionClass}
          </Text>
          <Text fontSize="sm" color={colors.primary['500']} mb={2}>
            {failure.exceptionMessage}
          </Text>
          {failure.stackTrace && (
            <Box
              as="pre"
              bgColor={colors.primary['200']}
              borderRadius="6px"
              p={3}
              fontSize="xs"
              maxHeight="14rem"
              overflow="auto"
              whiteSpace="pre"
            >
              {failure.stackTrace}
            </Box>
          )}
        </Box>
      ) : (
        <Muted>This instance has never failed.</Muted>
      )}
    </>
  );
};

const HistorySection: React.FC<{ instance: InstanceDetail }> = ({
  instance,
}) => {
  const navigate = useNavigate();
  if (!instance.history) {
    return null;
  }

  const runs = instance.history.recentRuns;
  return (
    <>
      <Section label="Recent history (this instance)" gated />
      {runs.length === 0 && <Muted>No runs logged yet.</Muted>}
      {runs.map((run) => (
        <RunRow key={run.id} run={run} />
      ))}
      <Link
        as="button"
        type="button"
        mt={2}
        fontSize="sm"
        color={colors.running['300']}
        onClick={() =>
          navigate('/history/all', {
            state: { taskName: instance.taskName, taskInstance: instance.id },
          })
        }
      >
        View full history →
      </Link>
    </>
  );
};

const RunRow: React.FC<{ run: InstanceRun }> = ({ run }) => (
  <HStack
    spacing={2}
    fontSize="sm"
    py={1}
    borderBottom="1px solid"
    borderBottomColor={colors.primary['200']}
  >
    <Text
      fontWeight="bold"
      color={run.succeeded ? colors.success['200'] : colors.failed['200']}
      minW="3rem"
    >
      {run.succeeded ? 'ok' : 'failed'}
    </Text>
    <Text
      color={colors.primary['500']}
      title={dateFormatText(new Date(run.timeStarted))}
    >
      {formatDistanceToNowStrict(new Date(run.timeStarted), {
        addSuffix: true,
      })}
    </Text>
    <Text color={colors.primary['400']}>
      {(run.durationMs / 1000).toFixed(1)}s
    </Text>
    <Text color={colors.primary['400']} noOfLines={1}>
      {run.exceptionMessage}
    </Text>
  </HStack>
);

// TaskAdminController is not registered when read-only=true, so hide the buttons entirely.
const Actions: React.FC<{
  instance: InstanceDetail;
  onDone: () => void;
  onDeleted: () => void;
}> = ({ instance, onDone, onDeleted }) => {
  const [busy, setBusy] = useState(false);
  const [confirmingDelete, setConfirmingDelete] = useState(false);
  const cancelRef = useRef<HTMLButtonElement>(null);
  const toast = useToast();

  if (getReadonly()) {
    return null;
  }

  const act = async (
    run: () => Promise<void>,
    message: string,
    after: () => void,
  ) => {
    setBusy(true);
    try {
      await run();
      toast({ title: message, status: 'success', duration: 3000 });
      after();
    } catch (error) {
      toast({
        title: error instanceof Error ? error.message : 'Action failed',
        status: 'error',
        duration: 5000,
      });
    } finally {
      setBusy(false);
    }
  };

  return (
    <HStack
      spacing={3}
      mt={8}
      pt={4}
      borderTop="1px solid"
      borderTopColor={colors.primary['300']}
    >
      <Button
        size="sm"
        colorScheme="blue"
        isDisabled={busy || instance.picked}
        onClick={() =>
          act(
            () => runTask(instance.id, instance.taskName),
            'Rerun scheduled',
            onDone,
          )
        }
      >
        Rerun now
      </Button>
      <Button
        size="sm"
        colorScheme="red"
        isDisabled={busy || instance.picked}
        onClick={() => setConfirmingDelete(true)}
      >
        Delete
      </Button>
      {instance.picked && (
        <Text fontSize="xs" color={colors.primary['400']}>
          Disabled while running
        </Text>
      )}
      {/* Deletion cannot be undone; spec.md marks it destructive and requires confirmation. */}
      <AlertDialog
        isOpen={confirmingDelete}
        leastDestructiveRef={cancelRef}
        onClose={() => setConfirmingDelete(false)}
      >
        <AlertDialogOverlay>
          <AlertDialogContent>
            <AlertDialogHeader fontSize="lg" fontWeight="bold">
              Delete instance
            </AlertDialogHeader>
            <AlertDialogBody>
              Delete {instance.taskName} · {instance.id}? The scheduled execution
              is removed and will not run.
            </AlertDialogBody>
            <AlertDialogFooter>
              <Button ref={cancelRef} onClick={() => setConfirmingDelete(false)}>
                Cancel
              </Button>
              <Button
                colorScheme="red"
                ml={3}
                isDisabled={busy}
                onClick={() => {
                  setConfirmingDelete(false);
                  act(
                    () => deleteTask(instance.id, instance.taskName),
                    'Instance deleted',
                    onDeleted,
                  );
                }}
              >
                Delete
              </Button>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialogOverlay>
      </AlertDialog>
    </HStack>
  );
};

const timestamp = (value: string | null) =>
  value ? dateFormatText(new Date(value)) : never();

const never = () => (
  <Text as="span" color={colors.primary['400']}>
    never
  </Text>
);

const notRunning = () => (
  <Text as="span" color={colors.primary['400']}>
    — (not running)
  </Text>
);
