import {
  View,
  Text,
  Image,
  TouchableOpacity,
  Pressable,
  ActivityIndicator,
  useColorScheme,
} from 'react-native';
import Ionicons from 'react-native-vector-icons/Ionicons';
import TrackPlayer, {
  useActiveMediaItem,
  useIsPlaying,
  useProgress,
  usePlaybackState,
} from '@rntp/player';
import { PlaybackState } from '@rntp/player';

const iconColors = {
  light: { foreground: '#0f0f0f', muted: '#737373' },
  dark: { foreground: '#f5f5f5', muted: '#999999' },
} as const;

const miniPlayerShadowStyle = {
  shadowColor: '#000',
  shadowOpacity: 0.2,
  shadowRadius: 12,
  shadowOffset: { width: 0, height: -2 },
  elevation: 12,
};

interface MiniPlayerProps {
  onPress: () => void;
}

export default function MiniPlayer({ onPress }: MiniPlayerProps) {
  const colorScheme = useColorScheme() === 'dark' ? 'dark' : 'light';
  const colors = iconColors[colorScheme];
  const activeMediaItem = useActiveMediaItem();
  const isPlaying = useIsPlaying();
  const playbackState = usePlaybackState();
  const { position, duration, cached } = useProgress(1);

  if (!activeMediaItem) return null;

  const progress = duration > 0 ? position / duration : 0;
  const cachedProgress = duration > 0 ? Math.min(cached / duration, 1) : 0;
  const artworkUrl =
    typeof activeMediaItem.artworkUrl === 'string'
      ? activeMediaItem.artworkUrl
      : undefined;

  return (
    <Pressable
      onPress={onPress}
      className="h-16 overflow-hidden border-t border-l border-r border-border bg-card shadow-lg"
      style={miniPlayerShadowStyle}
    >
      {/* Progress bar slot: always present to avoid layout shift when switching music/live or when duration loads */}
      <View className="h-0.5 bg-muted">
        {cachedProgress > 0 && (
          <View
            className="absolute h-full bg-primary/30"
            style={{ width: `${cachedProgress * 100}%` }}
          />
        )}
        <View
          className="h-full bg-primary"
          style={{ width: `${progress * 100}%` }}
        />
      </View>

      <View className="flex-1 flex-row items-center px-3">
        {/* Artwork */}
        {artworkUrl ? (
          <Image
            source={{ uri: artworkUrl }}
            className="w-11 h-11 rounded-lg"
          />
        ) : (
          <View className="w-11 h-11 rounded-lg bg-muted items-center justify-center">
            <Ionicons name="musical-notes" size={22} color={colors.muted} />
          </View>
        )}

        {/* Item info */}
        <View className="flex-1 mx-3">
          <Text
            className="text-sm font-semibold text-foreground"
            numberOfLines={1}
          >
            {activeMediaItem.title ?? 'Unknown'}
          </Text>
          <Text
            className="text-xs text-muted-foreground mt-0.5"
            numberOfLines={1}
          >
            {activeMediaItem.artist ?? ''}
          </Text>
        </View>

        {/* Controls */}
        <View className="flex-row items-center">
          <TouchableOpacity
            className="w-10 h-10 items-center justify-center"
            onPress={e => {
              e.stopPropagation();
              if (isPlaying) TrackPlayer.pause();
              else TrackPlayer.play();
            }}
            activeOpacity={0.6}
          >
            {playbackState === PlaybackState.Buffering ? (
              <ActivityIndicator size="small" color={colors.foreground} />
            ) : (
              <Ionicons
                name={isPlaying ? 'pause' : 'play'}
                size={24}
                color={colors.foreground}
              />
            )}
          </TouchableOpacity>
          <TouchableOpacity
            className="w-9 h-10 items-center justify-center"
            onPress={e => {
              e.stopPropagation();
              TrackPlayer.skipToNext();
            }}
            activeOpacity={0.6}
          >
            <Ionicons
              name="play-skip-forward"
              size={22}
              color={colors.foreground}
            />
          </TouchableOpacity>
        </View>
      </View>
    </Pressable>
  );
}
