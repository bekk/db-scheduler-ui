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
import { ArrowForwardIcon } from '@chakra-ui/icons';
import {
  Button,
  HStack,
  Input,
  InputGroup,
  InputLeftElement,
} from '@chakra-ui/react';
import colors from 'src/styles/colors';
import { useState } from 'react';

/**
 * Opens an instance by its exact id. db-scheduler can look an instance up by id but not search
 * them, so the box says so rather than looking like a search field.
 */
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
      <InputGroup size="sm" width="18rem">
        <InputLeftElement pointerEvents="none">
          <ArrowForwardIcon color={colors.primary['400']} />
        </InputLeftElement>
        <Input
          bgColor="white"
          borderRadius="6px"
          placeholder="Exact instance id"
          aria-label="Open instance by exact id"
          title="Opens one instance by its exact id. Partial ids are not searched."
          value={value}
          onChange={(e) => setValue(e.target.value)}
        />
      </InputGroup>
      <Button size="sm" type="submit" variant="outline">
        Open
      </Button>
    </HStack>
  );
};
