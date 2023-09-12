import { Button } from '@chakra-ui/react';
import runTask from 'src/services/runTask';
import { PlayIcon, RepeatIcon } from '../assets/icons';
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Task } from 'src/models/Task';
import { isStatus } from 'src/utils/determineStatus';

interface TaskRunButtonProps extends Task {
  style?: React.CSSProperties;
  refetch: () => void;
}
export const TaskRunButton: React.FC<TaskRunButtonProps> = (props) => {
  const { taskInstance, taskName, pickedBy, style, refetch } = props;
  const navigate = useNavigate();
  return (
    <>
      {!pickedBy[0] && (
        <Button
          style={style}
          onClick={(event) => {
            event.stopPropagation();
            !isStatus('Group', props)
              ? runTask(taskInstance[0], taskName).then(() => refetch())
              : navigate(`/${taskName}`, {
                  state: { taskName: taskName },
                });
          }}
          iconSpacing={2}
          width={100}
          bgColor={isStatus('Failed', props) ? '#5068F6' : '#E9ECFE'}
          textColor={isStatus('Failed', props) ? '#FFFFFF' : '#002FA7'}
          _hover={{
            bgColor: isStatus('Failed', props) ? '#344ACC' : '#D3D9FE',
          }}
          _active={{
            bgColor: isStatus('Failed', props) ? '#8a94c0' : '#eceefa',
          }}
          fontWeight="normal"
          leftIcon={
            isStatus('Group', props) ? undefined : isStatus('Failed', props) ? (
              <RepeatIcon boxSize={6} />
            ) : (
              <PlayIcon boxSize={4} />
            )
          }
        >
          {isStatus('Group', props)
            ? 'Show all'
            : isStatus('Failed', props)
            ? 'Rerun'
            : 'Run'}
        </Button>
      )}
    </>
  );
};
