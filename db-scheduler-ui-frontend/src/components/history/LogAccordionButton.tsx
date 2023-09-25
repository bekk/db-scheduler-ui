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
        overflowX="auto"
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
