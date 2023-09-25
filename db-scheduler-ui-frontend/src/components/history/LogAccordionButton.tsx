import { AccordionButton, AccordionIcon, Box, HStack } from '@chakra-ui/react';
import { LogStatus } from 'src/components/history/LogStatus';
import { dateFormatText } from 'src/utils/dateFormatText';
import React from 'react';
import { useParams } from 'react-router-dom';
import { AttachmentIcon } from '@chakra-ui/icons';

interface LogAccordionButtonProps {
  succeeded: boolean;
  id: number;
  taskName: string;
  taskData: object | null;
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
        overflowX="scroll"
        justifyContent={'space-between'}
        cursor={'pointer'}
        as={'div'}
      >
        <HStack w={'100%'} spacing={5}>
          <Box flex="1" display="inline-flex" alignItems={'center'}>
            <LogStatus succeeded={props.succeeded} />
            {props.taskData != null && (
              <AttachmentIcon
                position={'relative'}
                style={{ marginLeft: '-25', marginBottom: '-26' }}
              />
            )}
          </Box>
          <Box flex="2" textAlign="left" minWidth={28} hidden={!!taskName}>
            {props.taskName}
          </Box>
          <Box flex="2" textAlign="left" minWidth={28}>
            {props.taskInstance}
          </Box>
          <Box flex={'2'} textAlign={'left'} minWidth={28}>
            {dateFormatText(new Date(props.timeFinished))}
          </Box>
          <Box flex="2" textAlign="left" minWidth={28}>
            {props.exceptionMessage}
          </Box>
          <AccordionIcon flex="0.2" />
        </HStack>
      </AccordionButton>
    </h2>
  );
};
