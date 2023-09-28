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
import { Box, ResponsiveValue } from '@chakra-ui/react';
import { Property } from 'csstype';
import colors from 'src/styles/colors';

interface NumberCircleProps {
  number: number | string;
  bgColor?: string;
  textColor?: string;
  position?: ResponsiveValue<Property.Position>;
  transform?: ResponsiveValue<Property.Transform>;
  style?: React.CSSProperties;
  top?: string | number;
}

export const NumberCircle: React.FC<NumberCircleProps> = ({
  number,
  bgColor,
  textColor,
  position = 'absolute',
  transform,
  style,
  top,
}) => {
  const powerOfTen = (number + '').length - 1;
  const isExpanded = 1 <= powerOfTen;

  const baseSize: number = 22;

  const width = isExpanded ? baseSize + 7 * powerOfTen : baseSize;

  const height: number = 22;

  const borderRadius = isExpanded
    ? `${baseSize / 2}px ${baseSize / 2}px ${baseSize / 2}px ${baseSize / 2}px`
    : '50%';
  const leftOffset = isExpanded ? (width - baseSize) / 2 : 0;

  return (
    <Box
      position={position}
      borderRadius={borderRadius}
      top={top ?? 0}
      right={`${leftOffset}px`}
      width={`${width}px`}
      height={`${height}px`}
      backgroundColor={bgColor ?? colors.running['100']}
      display="flex"
      justifyContent="center"
      alignItems={'center'}
      color={textColor ?? colors.primary['100']}
      transform={transform}
      fontSize={'sm'}
      style={style}
    >
      {number}
    </Box>
  );
};
