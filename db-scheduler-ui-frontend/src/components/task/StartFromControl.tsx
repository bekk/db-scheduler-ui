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
  subDays,
  subHours,
} from 'date-fns';
import { useState } from 'react';
import colors from 'src/styles/colors';
import { dateFormatText } from 'src/utils/dateFormatText';

// datetime-local works in local time without a zone; the URL keeps an ISO instant.
const toInputValue = (iso: string | null) =>
  iso ? format(new Date(iso), "yyyy-MM-dd'T'HH:mm:ss") : '';

/** Where in execution time the list starts. Cleared means the earliest execution. */
export const StartFromControl: React.FC<{
  from: string | null;
  onChange: (from: string | null) => void;
}> = ({ from, onChange }) => {
  const { isOpen, onOpen, onClose } = useDisclosure();
  const [draft, setDraft] = useState(toInputValue(from));

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
          From: {from ? dateFormatText(new Date(from)) : 'earliest'}
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
                choose(draft ? new Date(draft).toISOString() : null);
              }}
            >
              <Input
                type="datetime-local"
                // One-second steps make the browser show and accept seconds.
                step={1}
                size="sm"
                value={draft}
                aria-label="Start from execution time"
                onChange={(e) => setDraft(e.target.value)}
              />
              <Button size="sm" type="submit">
                Go
              </Button>
            </HStack>
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
