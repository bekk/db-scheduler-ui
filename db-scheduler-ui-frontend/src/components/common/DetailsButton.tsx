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
import { Button } from '@chakra-ui/react';
import { PanelIcon } from 'src/assets/icons';
import colors from 'src/styles/colors';

/** Opens an instance in the side panel. Sits inside a clickable row that does the same. */
export const DetailsButton: React.FC<{
  label: string;
  onOpen: () => void;
}> = ({ label, onOpen }) => (
  <Button
    size="xs"
    variant="solid"
    bgColor="#eef1ff"
    color={colors.running['300']}
    _hover={{ bgColor: colors.running['100'] }}
    leftIcon={<PanelIcon />}
    aria-label={label}
    onClick={(event) => {
      // The row handles the same click; without this it would fire twice.
      event.stopPropagation();
      onOpen();
    }}
  >
    Details
  </Button>
);
