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
  Input,
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
  isValid,
  parse,
  subDays,
  subHours,
} from 'date-fns';
import { useState } from 'react';
import colors from 'src/styles/colors';

// Typed and shown in local time; the URL keeps an ISO instant.
const DISPLAY_FORMAT = 'yyyy.MM.dd HH:mm:ss';
// Shorter forms are accepted too, read as the start of that minute or day.
const INPUT_FORMATS = [DISPLAY_FORMAT, 'yyyy.MM.dd HH:mm', 'yyyy.MM.dd'];

const toInputValue = (iso: string | null) =>
  iso ? format(new Date(iso), DISPLAY_FORMAT) : '';

/** The instant typed, or null when it matches none of the accepted formats. */
function parseInput(text: string): Date | null {
  const trimmed = text.trim();
  for (const pattern of INPUT_FORMATS) {
    const date = parse(trimmed, pattern, new Date());
    if (isValid(date) && format(date, pattern) === trimmed) {
      return date;
    }
  }
  return null;
}

/** Where in execution time the list starts. Cleared means the earliest execution. */
export const StartFromControl: React.FC<{
  from: string | null;
  onChange: (from: string | null) => void;
}> = ({ from, onChange }) => {
  const { isOpen, onOpen, onClose } = useDisclosure();
  const [draft, setDraft] = useState(toInputValue(from));
  const typed = draft.trim() === '' ? null : parseInput(draft);
  const invalid = draft.trim() !== '' && typed === null;

  const choose = (value: string | null) => {
    onChange(value);
    onClose();
  };

  return (
    <Popover
      isOpen={isOpen}
      onOpen={() => {
        setDraft(toInputValue(from));
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
              <Input
                size="sm"
                fontFamily="mono"
                value={draft}
                placeholder="yyyy.mm.dd hh:mm:ss"
                aria-label="Start from execution time"
                isInvalid={invalid}
                onChange={(e) => setDraft(e.target.value)}
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
              yyyy.mm.dd, optionally with hh:mm or hh:mm:ss (24-hour, local
              time)
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
