import { Log } from 'src/models/Log';
import { AccordionItem, Divider } from '@chakra-ui/react';
import { LogAccordionButton } from 'src/components/history/LogAccordionButton';
import { LogAccordionItem } from 'src/components/history/LogAccordionItem';

interface LogCardProps {
  log: Log;
}

export const LogCard: React.FC<LogCardProps> = ({ log }) => {
  console.log(log.timeFinished);
  return (
    <AccordionItem
      backgroundColor={'#FFFFFF'}
      borderRadius={4}
      m={1}
      borderWidth={1}
      borderColor={'#E0E0E0'}
    >
      <LogAccordionButton
        succeeded={log.succeeded}
        id={log.id}
        taskInstance={log.taskInstance}
        taskName={log.taskName}
        exceptionClass={log.exceptionClass}
        exceptionMessage={log.exceptionMessage}
        timeFinished={log.timeFinished}
      />
      <Divider color={'#E0E0E0'} />
      <LogAccordionItem
        taskData={log.taskData}
        stackTrace={log.exceptionStackTrace}
      />
    </AccordionItem>
  );
};
