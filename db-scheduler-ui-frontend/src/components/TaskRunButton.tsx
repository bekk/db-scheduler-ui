import { Button } from '@chakra-ui/react';
import runTask from 'src/services/runTask';
import { PlayIcon, RepeatIcon } from '../assets/icons';
import React from 'react';

interface TaskRunButtonProps {
  taskInstance: string;
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
}) => (
  <>
    {!picked && (
      <Button
        style={style}
        onClick={(event) => {
          event.stopPropagation();
          runTask(taskInstance, taskName).then(() => refetch());
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
          consecutiveFailures > 0 ? (
            <RepeatIcon boxSize={6} />
          ) : (
            <PlayIcon boxSize={4} />
          )
        }
      >
        {consecutiveFailures > 0 ? 'Rerun' : 'Run'}
      </Button>
    )}
  </>
);
