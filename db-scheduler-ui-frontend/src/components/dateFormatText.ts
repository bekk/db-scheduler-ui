export function dateFormatText(date: Date) {
  return new Intl.DateTimeFormat('no-NO', {
    dateStyle: 'medium',
    timeStyle: 'medium',
  }).format(date);
}
