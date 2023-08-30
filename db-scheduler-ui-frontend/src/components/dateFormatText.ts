import { format } from 'date-fns';
export function dateFormatText(date: Date) {
  return format(date, 'dd. MMM yy, H:mm:ss');
}
