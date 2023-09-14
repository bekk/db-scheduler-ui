import { extendTheme } from '@chakra-ui/react';
import colors from 'src/styles/colors';

export const theme = extendTheme({
  styles: {
    global: {
      body: {
        bg: colors.primary['200'],
      },
    },
  },
});
