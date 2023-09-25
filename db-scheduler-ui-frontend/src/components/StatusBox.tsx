import { Box } from '@chakra-ui/react';
import { NumberCircle } from 'src/components/NumberCircle';
import colors from 'src/styles/colors';

interface StatusBoxProps {
  status: string;
  consecutiveFailures: number;
}

const statusColors: Record<
  string,
  { borderColor: string; backgroundColor: string; color: string }
> = {
  Failed: {
    borderColor: colors.failed['200'],
    backgroundColor: colors.failed['100'],
    color: colors.failed['200'],
  },
  Running: {
    borderColor: colors.running['100'],
    backgroundColor: colors.running['100'],
    color: colors.primary['900'],
  },
  Scheduled: {
    borderColor: colors.primary['300'],
    backgroundColor: colors.primary['200'],
    color: colors.primary['900'],
  },
  Group: {
    borderColor: colors.primary['900'],
    backgroundColor: colors.primary['200'],
    color: colors.primary['900'],
  },
};

export const StatusBox: React.FC<StatusBoxProps> = ({
  status,
  consecutiveFailures,
}) => {
  const statusInfo =
    status === 'Group'
      ? statusColors['Group']
      : statusColors[status] || statusColors['Scheduled'];
  const { borderColor, backgroundColor, color } = statusInfo;

  return (
    <Box
      textAlign={'center'}
      borderRadius={4}
      mr={4}
      width={115}
      borderColor={borderColor}
      backgroundColor={backgroundColor}
      color={color}
      px={4}
      py={1}
      borderWidth={1}
      position="relative"
      borderStyle={status !== 'Group' ? 'solid' : 'dashed'}
    >
      {consecutiveFailures > 0 && status !== 'Group' ? (
        <NumberCircle
          number={consecutiveFailures}
          bgColor={colors.failed['200']}
          transform={'translate(50%, -50%)'}
        ></NumberCircle>
      ) : (
        <></>
      )}

      {status}
    </Box>
  );
};
