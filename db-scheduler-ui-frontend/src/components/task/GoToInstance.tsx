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
import { Button, HStack, Input } from '@chakra-ui/react';
import { useState } from 'react';

/** Opens an instance by its exact id; the drawer reports an id that does not exist. */
export const GoToInstance: React.FC<{ onOpen: (id: string) => void }> = ({
  onOpen,
}) => {
  const [value, setValue] = useState('');

  return (
    <HStack
      as="form"
      spacing={2}
      onSubmit={(e: React.FormEvent) => {
        e.preventDefault();
        const id = value.trim();
        if (id) {
          onOpen(id);
        }
      }}
    >
      <Input
        size="sm"
        width="16rem"
        placeholder="Go to instance id"
        aria-label="Instance id"
        value={value}
        onChange={(e) => setValue(e.target.value)}
      />
      <Button size="sm" type="submit" variant="outline">
        Open
      </Button>
    </HStack>
  );
};
