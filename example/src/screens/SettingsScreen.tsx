import {
  View,
  Text,
  TouchableOpacity,
  useColorScheme,
  Alert,
  FlatList,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import Ionicons from 'react-native-vector-icons/Ionicons';
import TrackPlayer from '@rntp/player';
import { useEventLogStore, type LogEntry } from '../stores/eventLog';

const sectionLabelColors = {
  light: { muted: '#737373' },
  dark: { muted: '#999999' },
} as const;

function formatTime(date: Date): string {
  return date.toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
}

function stripEventPrefix(event: string): string {
  return event.replace(/^event\./, '');
}

function LogItem({ item, mutedColor }: { item: LogEntry; mutedColor: string }) {
  return (
    <View className="px-4 py-2 border-b border-border/30">
      <View className="flex-row items-center justify-between">
        <Text
          className="text-sm font-medium text-foreground flex-1"
          numberOfLines={1}
        >
          {stripEventPrefix(item.event)}
        </Text>
        <Text className="text-xs ml-2" style={{ color: mutedColor }}>
          {formatTime(item.timestamp)}
        </Text>
      </View>
      {item.payload !== '' && item.payload !== '{}' && (
        <Text
          className="text-xs mt-1"
          style={{ color: mutedColor }}
          numberOfLines={2}
        >
          {item.payload}
        </Text>
      )}
    </View>
  );
}

export default function SettingsScreen() {
  const insets = useSafeAreaInsets();
  const colorScheme = useColorScheme() === 'dark' ? 'dark' : 'light';
  const colors = sectionLabelColors[colorScheme];
  const logs = useEventLogStore(s => s.logs);
  const clearLogs = useEventLogStore(s => s.clearLogs);

  const handleClearCache = () => {
    Alert.alert(
      'Clear cache',
      'Remove all downloaded audio? Next playback will re-download.',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Clear',
          style: 'destructive',
          onPress: () => TrackPlayer.clearCache(),
        },
      ],
    );
  };

  return (
    <View
      className="flex-1 bg-background"
      style={{ paddingTop: insets.top + 8 }}
    >
      <View style={{ paddingHorizontal: 20 }}>
        <Text
          className="text-2xl font-semibold text-foreground mb-6"
          style={{ paddingLeft: 4 }}
        >
          Settings
        </Text>

        <View
          className="rounded-xl bg-card overflow-hidden"
          style={{ elevation: 0, shadowOpacity: 0 }}
        >
          <View className="border-b border-border/60 px-4 py-3">
            <Text
              className="text-xs font-medium uppercase tracking-wide"
              style={{ color: colors.muted }}
            >
              Cache
            </Text>
          </View>
          <TouchableOpacity
            className="flex-row items-center px-4 py-4 active:opacity-70"
            onPress={handleClearCache}
            activeOpacity={1}
          >
            <Ionicons name="trash-outline" size={22} color={colors.muted} />
            <Text className="text-base text-foreground ml-3">Clear cache</Text>
          </TouchableOpacity>
        </View>

        <Text
          className="text-xs text-muted-foreground mt-4 px-1"
          style={{ color: colors.muted }}
        >
          Clears downloaded audio. Next playback will re-download.
        </Text>
      </View>

      <View className="flex-1 mt-6" style={{ paddingHorizontal: 20 }}>
        <View
          className="rounded-xl bg-card overflow-hidden flex-1"
          style={{ elevation: 0, shadowOpacity: 0 }}
        >
          <View className="flex-row items-center justify-between border-b border-border/60 px-4 py-3">
            <Text
              className="text-xs font-medium uppercase tracking-wide"
              style={{ color: colors.muted }}
            >
              Event Log
            </Text>
            {logs.length > 0 && (
              <TouchableOpacity onPress={clearLogs} activeOpacity={0.7}>
                <Text className="text-xs" style={{ color: colors.muted }}>
                  Clear
                </Text>
              </TouchableOpacity>
            )}
          </View>
          {logs.length === 0 ? (
            <View className="flex-1 items-center justify-center py-8">
              <Text className="text-sm" style={{ color: colors.muted }}>
                No events yet. Start playing to see events.
              </Text>
            </View>
          ) : (
            <FlatList
              data={logs}
              keyExtractor={item => String(item.id)}
              renderItem={({ item }) => (
                <LogItem item={item} mutedColor={colors.muted} />
              )}
              contentContainerStyle={{ paddingBottom: insets.bottom }}
            />
          )}
        </View>
      </View>
    </View>
  );
}
