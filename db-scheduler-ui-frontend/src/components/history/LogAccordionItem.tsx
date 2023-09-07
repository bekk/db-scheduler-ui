import { AccordionPanel, Box, VStack } from '@chakra-ui/react';
interface LogAccordionItemProps {
  taskData: string | null;
  stackTrace: string | null;
}

export const LogAccordionItem: React.FC<LogAccordionItemProps> = ({
  taskData,
  stackTrace,
}) => {
  console.log(taskData);
  return (
    <AccordionPanel>
      <Box display={'flex'} justifyContent={'space-between'}></Box>
      <VStack
        align="start"
        spacing={2}
        bgColor={'#FFFFFF'}
        p={2}
        w={'100%'}
        borderRadius={4}
      >
        <Box>{stackTrace}</Box>
      </VStack>
    </AccordionPanel>
  );
};
