// Override or extend ChakraUI components here

import { ComponentStyleConfig } from '@chakra-ui/react';

const buttonStyles: ComponentStyleConfig = {
  baseStyle: {
    fontWeight: 'bold',
  },
  variants: {
    primary: (props) => ({
      bg: props.colorMode === 'dark' ? 'white' : 'primary.500',
    }),
  },
};

const components = {
  Button: buttonStyles,
};

export default components;
