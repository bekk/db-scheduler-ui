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
import { ChevronDownIcon, TimeIcon } from '@chakra-ui/icons';
import {
  Button,
  HStack,
  Popover,
  PopoverBody,
  PopoverContent,
  PopoverTrigger,
  Stack,
  Text,
  useDisclosure,
} from '@chakra-ui/react';
import {
  addDays,
  addHours,
  addWeeks,
  format,
  subDays,
  subHours,
} from 'date-fns';
import { useState } from 'react';
import { DateTimeField } from 'src/components/task/DateTimeField';
import colors from 'src/styles/colors';
import { EMPTY_PARTS, dateOf, partsOf } from 'src/utils/dateTimeParts';

// Shown in local time; the URL keeps an ISO instant.
const DISPLAY_FORMAT = 'yyyy.MM.dd HH:mm:ss';

const partsFor = (iso: string | null) =>
  iso ? partsOf(new Date(iso)) : EMPTY_PARTS;

/** Where in execution time the list starts. Cleared means the earliest execution. */
export const StartFromControl: React.FC<{
  from: string | null;
  onChange: (from: string | null) => void;
}> = ({ from, onChange }) => {
  const { isOpen, onOpen, onClose } = useDisclosure();
  const [parts, setParts] = useState(partsFor(from));
  const typed = dateOf(parts);
  const empty = Object.values(parts).every((v) => v === '');
  const invalid = !empty && typed === null;

  const choose = (value: string | null) => {
    onChange(value);
    onClose();
  };

  return (
    <Popover
      isOpen={isOpen}
      onOpen={() => {
        setParts(partsFor(from));
        onOpen();
      }}
      onClose={onClose}
      placement="bottom-start"
    >
      <PopoverTrigger>
        <Button
          size="sm"
          variant="outline"
          bgColor="white"
          fontWeight="normal"
          leftIcon={<TimeIcon color={colors.primary['400']} />}
          rightIcon={<ChevronDownIcon />}
        >
          From: {from ? format(new Date(from), DISPLAY_FORMAT) : 'earliest'}
        </Button>
      </PopoverTrigger>
      <PopoverContent width="30rem">
        <PopoverBody>
          <Stack spacing={3}>
            <PresetRow
              label="Forward"
              presets={[
                ['now', () => new Date()],
                ['+1 hour', () => addHours(new Date(), 1)],
                ['+6 hours', () => addHours(new Date(), 6)],
                ['+1 day', () => addDays(new Date(), 1)],
                ['+1 week', () => addWeeks(new Date(), 1)],
              ]}
              onPick={(date) => choose(date.toISOString())}
            />
            <HStack spacing={2} align="center">
              <Text fontSize="xs" color={colors.primary['400']} width="3.75rem">
                Back
              </Text>
              <Button size="xs" variant="outline" onClick={() => choose(null)}>
                earliest
              </Button>
              <Button
                size="xs"
                variant="outline"
                onClick={() => choose(subDays(new Date(), 1).toISOString())}
              >
                −1 day
              </Button>
              <Button
                size="xs"
                variant="outline"
                onClick={() => choose(subHours(new Date(), 1).toISOString())}
              >
                −1 hour
              </Button>
            </HStack>
            <Text fontSize="xs" color={colors.primary['400']}>
              Or a point in time
            </Text>
            <HStack
              as="form"
              spacing={2}
              onSubmit={(e: React.FormEvent) => {
                e.preventDefault();
                if (!invalid) {
                  choose(typed ? typed.toISOString() : null);
                }
              }}
            >
              <DateTimeField
                parts={parts}
                onChange={setParts}
                invalid={invalid}
              />
              <Button size="sm" type="submit" isDisabled={invalid}>
                Go
              </Button>
            </HStack>
            <Text
              fontSize="xs"
              color={invalid ? colors.failed['200'] : colors.primary['400']}
              mt={-1}
            >
              {invalid
                ? 'Not a valid date and time'
                : 'yyyy.mm.dd hh:mm:ss, 24-hour local time · ↑↓ change a field'}
            </Text>
          </Stack>
        </PopoverBody>
      </PopoverContent>
    </Popover>
  );
};

const PresetRow: React.FC<{
  label: string;
  presets: [string, () => Date][];
  onPick: (date: Date) => void;
}> = ({ label, presets, onPick }) => (
  <HStack spacing={2} align="center" flexWrap="wrap">
    <Text fontSize="xs" color={colors.primary['400']} width="3.75rem">
      {label}
    </Text>
    {presets.map(([text, at]) => (
      <Button
        key={text}
        size="xs"
        variant="outline"
        onClick={() => onPick(at())}
      >
        {text}
      </Button>
    ))}
  </HStack>
);
