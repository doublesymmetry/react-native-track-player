import { useCallback } from 'react';
import {
  View,
  Text,
  Image,
  ScrollView,
  Pressable,
  ActivityIndicator,
  useColorScheme,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import Ionicons from 'react-native-vector-icons/Ionicons';
import TrackPlayer, {
  PlayerCommand,
  useActiveMediaItem,
  useIsPlaying,
  usePlaybackState,
} from '@rntp/player';
import { PlaybackState } from '@rntp/player';
import { usePlaybackSourceStore } from '../stores/playbackSource';
import { radioStations } from '../data/radio';
import { cn } from '../lib/cn';

const iconColors = {
  light: { onPrimary: '#fff', muted: '#737373' },
  dark: { onPrimary: '#fff', muted: '#999999' },
} as const;

export default function RadioScreen() {
  const insets = useSafeAreaInsets();
  const colorScheme = useColorScheme() === 'dark' ? 'dark' : 'light';
  const colors = iconColors[colorScheme];
  const activeMediaItem = useActiveMediaItem();
  const isPlaying = useIsPlaying();
  const playbackState = usePlaybackState();

  const setSource = usePlaybackSourceStore(s => s.setSource);

  const playStation = useCallback(
    (index: number) => {
      const station = radioStations[index];
      if (!station) return;
      TrackPlayer.setPlaybackSpeed(1);
      setSource('radio');
      TrackPlayer.setCommands({
        capabilities: [PlayerCommand.PlayPause],
      });
      TrackPlayer.setMediaItem(station.mediaItem);
      TrackPlayer.play();
    },
    [setSource],
  );

  const contentContainerStyle = {
    paddingTop: insets.top + 16,
    paddingBottom: 32,
  };

  return (
    <ScrollView
      className="flex-1 bg-background"
      contentContainerStyle={contentContainerStyle}
    >
      {/* Section Header */}
      <View className="px-6 pb-4">
        <Text className="text-2xl font-bold text-foreground">Live Radio</Text>
        <Text className="text-sm text-muted-foreground mt-1">
          Tune in to live stations around the world
        </Text>
      </View>

      <View className="px-4">
        {radioStations.map((station, index) => {
          const isActive =
            activeMediaItem?.mediaId === station.mediaItem.mediaId;
          return (
            <Pressable
              key={station.mediaItem.mediaId}
              className={cn(
                'mb-3 overflow-hidden rounded-2xl',
                isActive ? 'bg-primary/5' : 'bg-secondary',
              )}
              onPress={() => {
                if (isActive && isPlaying) {
                  TrackPlayer.pause();
                } else if (isActive) {
                  TrackPlayer.play();
                } else {
                  playStation(index);
                }
              }}
            >
              {/* Color accent bar at top when active */}
              {isActive && (
                <View
                  className="h-[3px] rounded-t-2xl"
                  style={{ backgroundColor: station.color }}
                />
              )}

              <View className="flex-row items-center p-4">
                <View className="shadow-md">
                  {typeof station.mediaItem.artworkUrl === 'string' ? (
                    <Image
                      source={{ uri: station.mediaItem.artworkUrl }}
                      className="w-[72px] h-[72px] rounded-xl"
                    />
                  ) : (
                    <View className="w-[72px] h-[72px] rounded-xl bg-muted items-center justify-center">
                      <Ionicons name="radio" size={32} color={colors.muted} />
                    </View>
                  )}
                </View>
                <View className="flex-1 ml-4">
                  <View className="flex-row items-center">
                    <Text
                      className={cn(
                        'text-base font-bold',
                        isActive ? 'text-primary' : 'text-foreground',
                      )}
                    >
                      {station.mediaItem.title}
                    </Text>
                    {isActive && isPlaying && (
                      <View className="ml-2 bg-red-500 px-1.5 py-0.5 rounded">
                        <Text className="text-white font-bold text-[9px]">
                          LIVE
                        </Text>
                      </View>
                    )}
                  </View>
                  <Text className="text-xs text-primary font-medium mt-0.5">
                    {station.genre}
                  </Text>
                  <Text
                    className="text-xs text-muted-foreground mt-1"
                    numberOfLines={1}
                  >
                    {station.description}
                  </Text>
                </View>

                {/* Play/Pause Button */}
                <View
                  className={cn(
                    'w-11 h-11 rounded-full items-center justify-center ml-2',
                    isActive ? 'bg-primary' : 'bg-muted',
                  )}
                >
                  {isActive && playbackState === PlaybackState.Buffering ? (
                    <ActivityIndicator size="small" color={colors.onPrimary} />
                  ) : (
                    <Ionicons
                      name={isActive && isPlaying ? 'pause' : 'play'}
                      size={22}
                      color={isActive ? colors.onPrimary : colors.muted}
                    />
                  )}
                </View>
              </View>
            </Pressable>
          );
        })}
      </View>
    </ScrollView>
  );
}
