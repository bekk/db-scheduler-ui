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
        useWeekdaysShort={true}
      ></DatePicker>
    </Box>
  );
};
