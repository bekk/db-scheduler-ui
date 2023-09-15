import DatePicker from 'react-datepicker';
import { Box } from '@chakra-ui/react';
import { useState } from 'react';
import 'react-datepicker/dist/react-datepicker.css';

interface DateTimeInputProps {
  defaultDate: Date | null;
}

export const DateTimeInput: React.FC<DateTimeInputProps> = ({
  defaultDate,
}) => {
  const [startDate, setStartDate] = useState(defaultDate);
  console.log(startDate);
  return (
    <Box>
      <DatePicker
        selected={startDate}
        onChange={(date) => setStartDate(date!)}
        showTimeSelect
        timeFormat={'HH:mm'}
      ></DatePicker>
    </Box>
  );
};
