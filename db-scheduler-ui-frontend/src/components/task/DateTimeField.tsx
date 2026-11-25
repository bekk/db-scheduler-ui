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
import { CalendarIcon } from '@chakra-ui/icons';
import { Box, HStack, IconButton, Input, Text } from '@chakra-ui/react';
import { format, isValid } from 'date-fns';
import { useRef } from 'react';
import colors from 'src/styles/colors';
import { DateTimeParts, dateOf, partsOf } from 'src/utils/dateTimeParts';

interface Segment {
  key: keyof DateTimeParts;
  digits: number;
  min: number;
  max: number;
  /** Separator drawn after this segment. */
  after: string;
}

// Always year first, whatever the browser locale: the native date-time field cannot be told
// what order to use, which is why this field exists.
const SEGMENTS: Segment[] = [
  { key: 'year', digits: 4, min: 1970, max: 9999, after: '.' },
  { key: 'month', digits: 2, min: 1, max: 12, after: '.' },
  { key: 'day', digits: 2, min: 1, max: 31, after: '' },
  { key: 'hour', digits: 2, min: 0, max: 23, after: ':' },
  { key: 'minute', digits: 2, min: 0, max: 59, after: ':' },
  { key: 'second', digits: 2, min: 0, max: 59, after: '' },
];

/** yyyy.mm.dd hh:mm:ss as separate number boxes, with the browser's calendar one click away. */
export const DateTimeField: React.FC<{
  parts: DateTimeParts;
  onChange: (parts: DateTimeParts) => void;
  invalid: boolean;
}> = ({ parts, onChange, invalid }) => {
  const refs = useRef<(HTMLInputElement | null)[]>([]);
  const native = useRef<HTMLInputElement>(null);

  const set = (index: number, value: string) => {
    onChange({ ...parts, [SEGMENTS[index].key]: value });
  };

  const step = (index: number, delta: number) => {
    const s = SEGMENTS[index];
    const current =
      parts[s.key] === ''
        ? delta > 0
          ? s.min - 1
          : s.max + 1
        : Number(parts[s.key]);
    const span = s.max - s.min + 1;
    const next = ((current - s.min + delta + span) % span) + s.min;
    set(index, String(next).padStart(s.digits, '0'));
  };

  return (
    <HStack
      spacing={0}
      border="1px solid"
      borderColor={invalid ? colors.failed['200'] : colors.primary['300']}
      borderRadius="6px"
      px={2}
      py={1}
      bgColor="white"
      fontFamily="mono"
      fontSize="sm"
      flex={1}
    >
      {SEGMENTS.map((s, index) => (
        <HStack key={s.key} spacing={0} mr={s.key === 'day' ? 3 : 0}>
          <Input
            ref={(el) => (refs.current[index] = el)}
            variant="unstyled"
            size="sm"
            width={`${s.digits + 0.6}ch`}
            textAlign="center"
            inputMode="numeric"
            placeholder={
              s.key === 'year' ? 'yyyy' : s.key.slice(0, 1).repeat(2)
            }
            aria-label={s.key}
            value={parts[s.key]}
            _focus={{ bgColor: colors.running['100'], borderRadius: '3px' }}
            onFocus={(e) => e.target.select()}
            // A click would otherwise drop the caret mid-value; a box is replaced, not edited.
            onMouseUp={(e) => {
              e.preventDefault();
              e.currentTarget.select();
            }}
            onChange={(e) => {
              const digits = e.target.value
                .replace(/\D/g, '')
                .slice(0, s.digits);
              set(index, digits);
              if (digits.length === s.digits) {
                refs.current[index + 1]?.focus();
              }
            }}
            onKeyDown={(e) => {
              if (/^\d$/.test(e.key)) {
                e.preventDefault();
                const el = e.currentTarget;
                const current = parts[s.key];
                const wholeSelected =
                  el.selectionStart === 0 && el.selectionEnd === current.length;
                const next =
                  wholeSelected || current.length >= s.digits
                    ? e.key
                    : current + e.key;
                set(index, next);
                if (next.length === s.digits) {
                  refs.current[index + 1]?.focus();
                }
              } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                step(index, 1);
              } else if (e.key === 'ArrowDown') {
                e.preventDefault();
                step(index, -1);
              } else if (e.key === 'Backspace' && parts[s.key] === '') {
                refs.current[index - 1]?.focus();
              } else if (
                e.key === 'ArrowRight' &&
                e.currentTarget.selectionStart === parts[s.key].length
              ) {
                refs.current[index + 1]?.focus();
              } else if (
                e.key === 'ArrowLeft' &&
                e.currentTarget.selectionStart === 0
              ) {
                refs.current[index - 1]?.focus();
              }
            }}
          />
          {s.after && <Text color={colors.primary['400']}>{s.after}</Text>}
        </HStack>
      ))}
      <Box flex={1} />
      <IconButton
        aria-label="Pick from a calendar"
        icon={<CalendarIcon />}
        size="xs"
        variant="ghost"
        onClick={() => {
          const picker = native.current;
          if (!picker) {
            return;
          }
          // Open the calendar on the date already in the boxes.
          const current = dateOf(parts);
          picker.value = current
            ? format(current, "yyyy-MM-dd'T'HH:mm:ss")
            : '';
          picker.showPicker?.();
        }}
      />
      {/* Only its calendar is used; the order it would display fields in is the browser's. */}
      <input
        ref={native}
        type="datetime-local"
        step={1}
        tabIndex={-1}
        aria-hidden="true"
        style={{
          position: 'absolute',
          width: 0,
          height: 0,
          opacity: 0,
          pointerEvents: 'none',
        }}
        onChange={(e) => {
          const date = e.target.value ? new Date(e.target.value) : null;
          if (date && isValid(date)) {
            onChange(partsOf(date));
          }
        }}
      />
    </HStack>
  );
};
