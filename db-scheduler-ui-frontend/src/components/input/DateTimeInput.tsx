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
import DatePicker from 'react-datepicker';
import { Box } from '@chakra-ui/react';
import 'react-datepicker/dist/react-datepicker.css';
import colors from 'src/styles/colors';

interface DateTimeInputProps {
  selectedDate: Date | null;
  onChange: (date: Date | null) => void;
}

export const DateTimeInput: React.FC<DateTimeInputProps> = ({
  selectedDate,
  onChange,
}) => {
  return (
    <Box
      p={1}
      borderRadius={4}
      borderColor={colors.primary[300]}
      borderWidth={1}
      backgroundColor={colors.primary[100]}
    >
      <DatePicker
        selected={selectedDate}
        onChange={onChange}
        showTimeSelect
        timeFormat={'HH:mm'}
        dateFormat={'yyyy-MM-dd HH:mm'}
        placeholderText={'YYYY-MM-DD HH:mm'}
        useWeekdaysShort
      ></DatePicker>
    </Box>
  );
};
