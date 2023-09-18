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
