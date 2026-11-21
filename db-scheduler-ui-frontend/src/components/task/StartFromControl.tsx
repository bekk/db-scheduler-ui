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
import { Button, HStack, Input, Text } from '@chakra-ui/react';
import { format, subDays, subHours } from 'date-fns';
import colors from 'src/styles/colors';

// datetime-local works in local time without a zone; the URL keeps an ISO instant.
const toInputValue = (iso: string | null) =>
  iso ? format(new Date(iso), "yyyy-MM-dd'T'HH:mm") : '';

/** Where in execution time the list starts. Cleared means the earliest execution. */
export const StartFromControl: React.FC<{
  from: string | null;
  onChange: (from: string | null) => void;
}> = ({ from, onChange }) => {
  const preset = (date: Date) => onChange(date.toISOString());

  return (
    <HStack spacing={2}>
      <Text fontSize="sm" color={colors.primary['400']} whiteSpace="nowrap">
        Start from
      </Text>
      <Input
        type="datetime-local"
        size="sm"
        width="13rem"
        value={toInputValue(from)}
        aria-label="Start from execution time"
        onChange={(e) =>
          onChange(
            e.target.value ? new Date(e.target.value).toISOString() : null,
          )
        }
      />
      <Button size="xs" variant="outline" onClick={() => preset(new Date())}>
        now
      </Button>
      <Button
        size="xs"
        variant="outline"
        onClick={() => preset(subHours(new Date(), 1))}
      >
        −1h
      </Button>
      <Button
        size="xs"
        variant="outline"
        onClick={() => preset(subDays(new Date(), 1))}
      >
        −1d
      </Button>
      <Button size="xs" variant="outline" onClick={() => onChange(null)}>
        earliest
      </Button>
    </HStack>
  );
};
