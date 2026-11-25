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
import { createIcon } from '@chakra-ui/icons';

/** A window with a panel on its right: marks actions that open the side panel. */
export const PanelIcon = createIcon({
  displayName: 'Panel',
  viewBox: '0 0 16 16',
  path: [
    <rect
      key="frame"
      x="1.5"
      y="2.5"
      width="13"
      height="11"
      rx="2"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
    />,
    <rect
      key="panel"
      x="9"
      y="3"
      width="5"
      height="10"
      fill="currentColor"
      opacity="0.35"
    />,
  ],
});
