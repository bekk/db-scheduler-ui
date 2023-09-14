import { AccordionButton, AccordionIcon, Box, HStack } from '@chakra-ui/react';
import { LogStatus } from 'src/components/history/LogStatus';
import { dateFormatText } from 'src/utils/dateFormatText';
import colors from 'src/styles/colors';
import React from 'react';
import { useParams } from 'react-router-dom';

interface LogAccordionButtonProps {
  succeeded: boolean;
  id: number;
  taskName: string;
  taskInstance: string;
  exceptionClass: string | null;
  exceptionMessage: string | null;
  timeFinished: Date;
}

export const LogAccordionButton: React.FC<LogAccordionButtonProps> = (
  props,
) => {
  const { taskName } = useParams();
  return (
    <h2>
      <AccordionButton
        justifyContent={'space-between'}
        _hover={{ backgroundColor: colors.primary['100'] }}
      >
        <HStack w={'100%'} spacing={5}>
          <Box flex="1" display="inline-flex">
            <LogStatus succeeded={props.succeeded} />
          </Box>
          <Box flex="2" textAlign="left" hidden={!!taskName}>
            {props.taskName}
          </Box>
          <Box flex="2" textAlign="left">
            {props.taskInstance}
          </Box>
          <Box flex={'2'} textAlign={'left'}>
            {dateFormatText(new Date(props.timeFinished))}
          </Box>
          <Box flex="2" textAlign="left">
            {props.exceptionMessage}
          </Box>
          <AccordionIcon flex="0.2" />
        </HStack>
      </AccordionButton>
    </h2>
  );
};
