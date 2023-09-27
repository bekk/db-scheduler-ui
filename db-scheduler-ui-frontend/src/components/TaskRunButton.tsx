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
import { Button } from '@chakra-ui/react';
import runTask from 'src/services/runTask';
import { PlayIcon, RepeatIcon } from '../assets/icons';
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Task } from 'src/models/Task';
import { isStatus } from 'src/utils/determineStatus';
import colors from 'src/styles/colors';
import { ArrowRightIcon } from '@chakra-ui/icons';

interface TaskRunButtonProps extends Task {
  style?: React.CSSProperties;
  refetch: () => void;
}

export const TaskRunButton: React.FC<TaskRunButtonProps> = (props) => {
  const { taskInstance, taskName, pickedBy, style, refetch } = props;
  const navigate = useNavigate();
  const buttonProps = {
    style,
    onClick: (event: React.MouseEvent) => {
      event.stopPropagation();
      !isStatus('Group', props)
        ? runTask(taskInstance[0], taskName).then(() => refetch())
        : navigate(`/${taskName}`, {
            state: { taskName: taskName },
          });
    },
    bgColor: isStatus('Group', props)
      ? 'transparent'
      : isStatus('Failed', props)
      ? colors.running['300']
      : colors.running['100'],
    textColor: isStatus('Group', props)
      ? colors.primary['600']
      : isStatus('Failed', props)
      ? colors.primary['100']
      : colors.running['500'],
    hoverBgColor: !isStatus('Group', props)
      ? isStatus('Failed', props)
        ? colors.running['400']
        : colors.running['200']
      : 'transparent',
    hoverTextColor: isStatus('Group', props)
      ? colors.primary['400']
      : undefined,
    activeBgColor: isStatus('Group', props)
      ? colors.primary['100']
      : isStatus('Failed', props)
      ? colors.running['300']
      : colors.running['100'],
    activeTextColor: isStatus('Group', props)
      ? colors.primary['500']
      : undefined,
    text: isStatus('Group', props)
      ? 'Show all'
      : isStatus('Failed', props)
      ? 'Rerun'
      : 'Run',
    leftIcon: isStatus('Group', props) ? undefined : isStatus(
        'Failed',
        props,
      ) ? (
      <RepeatIcon boxSize={6} />
    ) : (
      <PlayIcon boxSize={4} />
    ),
    rightIcon: isStatus('Group', props) ? (
      <ArrowRightIcon boxSize={3} />
    ) : undefined,
  };

  return <>{!pickedBy[0] && <IconButton {...buttonProps} />}</>;
};
interface IconButtonProps {
  style?: React.CSSProperties;
  onClick: (event: React.MouseEvent) => void;
  bgColor: string;
  textColor: string;
  hoverBgColor: string;
  hoverTextColor: string | undefined;
  activeBgColor: string;
  activeTextColor: string | undefined;
  leftIcon?: React.ReactElement;
  rightIcon?: React.ReactElement;
  text: string;
}

export const IconButton: React.FC<IconButtonProps> = (props) => {
  const {
    style,
    onClick,
    bgColor,
    textColor,
    hoverBgColor,
    hoverTextColor,
    activeBgColor,
    activeTextColor,
    leftIcon,
    rightIcon,
    text,
  } = props;

  return (
    <Button
      style={style}
      onClick={onClick}
      iconSpacing={2}
      width={100}
      bgColor={bgColor}
      textColor={textColor}
      _hover={{
        bgColor: hoverBgColor,
        textColor: hoverTextColor,
      }}
      _active={{
        bgColor: activeBgColor,
        textColor: activeTextColor,
      }}
      fontWeight="normal"
      rightIcon={rightIcon}
      leftIcon={leftIcon}
    >
      {text}
    </Button>
  );
};
