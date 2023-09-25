import { AccordionPanel, Box, VStack } from '@chakra-ui/react';
import colors from 'src/styles/colors';
import { LogDataRow } from 'src/components/history/LogDataRow';
interface LogAccordionItemProps {
  taskData: object | null;
  stackTrace: string | null;
}

export const LogAccordionItem: React.FC<LogAccordionItemProps> = ({
  stackTrace,
  taskData,
}) => {
  return (
    <AccordionPanel overflowX="auto">
      <Box display={'flex'} justifyContent={'space-between'}></Box>
      <VStack
        align="start"
        spacing={2}
        bgColor={colors.primary['100']}
        p={2}
        w={'100%'}
        borderRadius={4}
      >
        <pre>{stackTrace}</pre>
        <LogDataRow taskData={taskData} />
      </VStack>
    </AccordionPanel>
  );
};
