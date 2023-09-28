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
import { Box } from '@chakra-ui/react';
import { useState } from 'react';
import { NumberCircle } from './NumberCircle';

type RefreshCircleProps = {
  number: number;
  color: string;
  textColor?: string;
  visible?: boolean;
  hoverText: string;
};

export const RefreshCircle: React.FC<RefreshCircleProps> = ({
  number,
  color,
  textColor,
  visible,
  hoverText,
}) => {
  const [hovered, setHovered] = useState(false);

  const text = hovered ? hoverText : '';

  const powerOfTen = (number + hoverText).length - 1;
  const isExpanded = 1 <= powerOfTen;
  const baseSize: number = 22;
  const width = isExpanded ? baseSize + 7 * powerOfTen : baseSize;

  const marginLeft = isExpanded ? -width : 0;

  return (
    <Box
      alignItems={'end'}
      display={'flex'}
      justifyContent={'flex-end'}
      overflow={'visible'}
      ml={marginLeft}
      position="relative"
      visibility={visible ? 'visible' : 'hidden'}
    >
      <Box
        onMouseEnter={() => setHovered(true)}
        onMouseLeave={() => setHovered(false)}
      >
        <NumberCircle
          number={number + text}
          bgColor={color}
          textColor={textColor}
          position="relative"
          top={'auto'}
          style={{ bottom: '0', left: '0' }}
        />
      </Box>
    </Box>
  );
};
