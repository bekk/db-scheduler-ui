import { AccordionPanel, Box, VStack } from '@chakra-ui/react';
import colors from 'src/styles/colors';
interface LogAccordionItemProps {
  taskData: object | null;
  stackTrace: string | null;
}

export const LogAccordionItem: React.FC<LogAccordionItemProps> = ({
  stackTrace,
}) => {
  return (
    <AccordionPanel>
      <Box display={'flex'} justifyContent={'space-between'}></Box>
      <VStack
        align="start"
        spacing={2}
        bgColor={colors.primary['100']}
        p={2}
        w={'100%'}
        borderRadius={4}
      >
        <Box>
          {stackTrace?.split('\n').map((str, index, array) =>
            index === array.length - 1 ? (
              str
            ) : (
              <>
                {str}
                <br />
              </>
            ),
          )}
        </Box>
      </VStack>
    </AccordionPanel>
  );
};
