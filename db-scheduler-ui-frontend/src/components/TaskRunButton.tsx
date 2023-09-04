import { Button } from '@chakra-ui/react';
import runTask from 'src/services/runTask';
import { PlayIcon, RepeatIcon } from '../assets/icons';
import React from 'react';
import { useNavigate } from 'react-router-dom';

interface TaskRunButtonProps {
  taskInstance: string[];
  taskName: string;
  picked: boolean;
  consecutiveFailures: number;
  style?: React.CSSProperties;
  refetch: () => void;
}
export const TaskRunButton: React.FC<TaskRunButtonProps> = ({
  taskInstance,
  taskName,
  picked,
  consecutiveFailures,
  style,
  refetch,
}) => {
  const navigate = useNavigate();
  return (
    <>
      {!picked && (
        <Button
          style={style}
          onClick={(event) => {
            event.stopPropagation();
            taskInstance.length === 1
              ? runTask(taskInstance[0], taskName).then(() => refetch())
              : navigate(`tasks/${taskName}`, {
                  state: { taskName: taskName },
                });
          }}
          iconSpacing={2}
          width={100}
          bgColor={consecutiveFailures > 0 ? '#5068F6' : '#E9ECFE'}
          textColor={consecutiveFailures > 0 ? '#FFFFFF' : '#002FA7'}
          _hover={{
            bgColor: consecutiveFailures > 0 ? '#344ACC' : '#D3D9FE',
          }}
          _active={{
            bgColor: consecutiveFailures > 0 ? '#8a94c0' : '#eceefa',
          }}
          fontWeight="normal"
          leftIcon={
            taskInstance.length > 1 ? (
              <></>
            ) : consecutiveFailures > 0 ? (
              <RepeatIcon boxSize={6} />
            ) : (
              <PlayIcon boxSize={4} />
            )
          }
        >
          {taskInstance.length > 1
            ? 'Show all'
            : consecutiveFailures > 0
            ? 'Rerun'
            : 'Run'}
        </Button>
      )}
    </>
  );
};
