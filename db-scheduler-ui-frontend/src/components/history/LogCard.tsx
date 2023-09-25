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
import { Log } from 'src/models/Log';
import { AccordionItem, Divider } from '@chakra-ui/react';
import { LogAccordionButton } from 'src/components/history/LogAccordionButton';
import { LogAccordionItem } from 'src/components/history/LogAccordionItem';
import colors from 'src/styles/colors';

interface LogCardProps {
  log: Log;
}

export const LogCard: React.FC<LogCardProps> = ({ log }) => (
  <AccordionItem
    backgroundColor={colors.primary['100']}
    borderRadius={4}
    m={1}
    borderWidth={1}
    borderColor={colors.primary['300']}
  >
    <LogAccordionButton
      succeeded={log.succeeded}
      id={log.id}
      taskInstance={log.taskInstance}
      taskName={log.taskName}
      taskData={log.taskData}
      exceptionClass={log.exceptionClass}
      exceptionMessage={log.exceptionMessage}
      timeFinished={log.timeFinished}
    />
    <Divider color={colors.primary['300']} />
    <LogAccordionItem
      taskData={log.taskData}
      stackTrace={log.exceptionStackTrace}
    />
  </AccordionItem>
);
