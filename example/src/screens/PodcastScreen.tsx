import { useCallback, useEffect, useState } from 'react';
import {
  View,
  Text,
  Image,
  ScrollView,
  TouchableOpacity,
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
import { useStrongSongsFeed } from '../hooks/useStrongSongsFeed';
import { formatDuration } from '../lib/formatTime';
import { cn } from '../lib/cn';

const SPEED_OPTIONS = [0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0];

const iconColors = {
  light: { onPrimary: '#fff', muted: '#737373' },
  dark: { onPrimary: '#fff', muted: '#999999' },
} as const;

export default function PodcastScreen() {
  const insets = useSafeAreaInsets();
  const colorScheme = useColorScheme() === 'dark' ? 'dark' : 'light';
  const colors = iconColors[colorScheme];
  const activeMediaItem = useActiveMediaItem();
  const isPlaying = useIsPlaying();
  const playbackState = usePlaybackState();
  const [speed, setSpeed] = useState(1.0);
  const { show, loading, error, refetch } = useStrongSongsFeed();

  const setSource = usePlaybackSourceStore(s => s.setSource);
  const playbackSource = usePlaybackSourceStore(s => s.source);

  useEffect(() => {
    if (playbackSource === 'podcast' && activeMediaItem) {
      setSpeed(TrackPlayer.getPlaybackSpeed());
    }
  }, [playbackSource, activeMediaItem]);

  const playEpisode = useCallback(
    (index: number) => {
      if (!show?.episodes?.length) return;
      setSource('podcast');
      TrackPlayer.setCommands({
        capabilities: [
          PlayerCommand.PlayPause,
          PlayerCommand.Next,
          PlayerCommand.Previous,
          PlayerCommand.Seek,
          PlayerCommand.SkipForward,
          PlayerCommand.SkipBackward,
        ],
        forwardInterval: 30,
        backwardInterval: 15,
      });
      TrackPlayer.setMediaItems(show.episodes, index);
      TrackPlayer.play();
    },
    [show?.episodes, setSource],
  );

  const cycleSpeed = useCallback(() => {
    const currentIdx = SPEED_OPTIONS.indexOf(speed);
    const nextIdx = (currentIdx + 1) % SPEED_OPTIONS.length;
    const nextSpeed = SPEED_OPTIONS[nextIdx] ?? 1.0;
    setSpeed(nextSpeed);
    TrackPlayer.setPlaybackSpeed(nextSpeed);
  }, [speed]);

  if (loading && !show) {
    return (
      <View
        className="flex-1 bg-background items-center justify-center"
        style={{ paddingTop: insets.top }}
      >
        <ActivityIndicator size="large" />
        <Text className="text-muted-foreground mt-3">
          Loading Strong Songs…
        </Text>
      </View>
    );
  }

  if (error && !show) {
    return (
      <View
        className="flex-1 bg-background items-center justify-center px-8"
        style={{ paddingTop: insets.top }}
      >
        <Text className="text-foreground font-semibold text-center">
          Couldn't load feed
        </Text>
        <Text className="text-muted-foreground text-sm text-center mt-2">
          {error.message}
        </Text>
        <TouchableOpacity
          className="bg-primary mt-6 px-6 py-3 rounded-full"
          onPress={() => refetch()}
          activeOpacity={0.8}
        >
          <Text className="text-primary-foreground font-semibold">Retry</Text>
        </TouchableOpacity>
      </View>
    );
  }

  if (!show) {
    return null;
  }

  const episodes = show.episodes ?? [];
  const contentContainerStyle = {
    paddingTop: insets.top + 16,
    paddingBottom: 32,
  };

  return (
    <ScrollView
      className="flex-1 bg-background"
      contentContainerStyle={contentContainerStyle}
    >
      {/* Show Header */}
      <View className="px-6 pb-5">
        <View className="flex-row">
          {show.artwork ? (
            <View className="shadow-lg">
              <Image
                source={{ uri: show.artwork }}
                className="w-[120px] h-[120px] rounded-2xl"
              />
            </View>
          ) : null}
          <View className="flex-1 ml-4 justify-center">
            <Text className="text-xl font-bold text-foreground">
              {show.title}
            </Text>
            <Text className="text-sm text-primary font-medium mt-1">
              {show.author}
            </Text>
            {show.description ? (
              <Text
                className="text-xs text-muted-foreground mt-2"
                numberOfLines={3}
              >
                {show.description}
              </Text>
            ) : null}
          </View>
        </View>
      </View>

      {/* Speed Control Bar */}
      <View className="flex-row items-center h-10 rounded-[20px] mx-5 mb-5 px-4 bg-secondary">
        <Text className="text-xs text-muted-foreground font-semibold flex-1">
          PLAYBACK SPEED
        </Text>
        <TouchableOpacity
          className="bg-primary items-center justify-center px-3.5 h-7 rounded-[14px]"
          onPress={cycleSpeed}
          activeOpacity={0.7}
        >
          <Text className="text-primary-foreground text-xs font-bold">
            {`${speed}x`}
          </Text>
        </TouchableOpacity>
      </View>

      {/* Section Header */}
      <View className="flex-row items-center justify-between px-6 mb-3">
        <Text className="text-xs text-muted-foreground font-semibold">
          EPISODES
        </Text>
        {loading ? (
          <ActivityIndicator size="small" />
        ) : (
          <TouchableOpacity onPress={() => refetch()}>
            <Text className="text-xs text-primary font-medium">Refresh</Text>
          </TouchableOpacity>
        )}
      </View>

      {/* Episodes */}
      <View className="px-4">
        {episodes.length === 0 && !loading ? (
          <Text className="text-muted-foreground text-sm py-4">
            No episodes in feed.
          </Text>
        ) : null}
        {episodes.map((episode, index) => {
          const isActive = activeMediaItem?.mediaId === episode.mediaId;
          const titleText =
            isActive && isPlaying
              ? `♫ ${episode.title ?? ''}`
              : (episode.title ?? '');
          return (
            <Pressable
              key={episode.mediaId ?? index}
              className={cn(
                'mb-2 rounded-2xl p-4',
                isActive ? 'bg-primary/5' : 'bg-secondary',
              )}
              onPress={() => {
                if (isActive && isPlaying) {
                  TrackPlayer.pause();
                } else if (isActive) {
                  TrackPlayer.play();
                } else {
                  playEpisode(index);
                }
              }}
            >
              <View className="flex-row items-start">
                <View className="flex-1">
                  <Text
                    className={cn(
                      'text-base font-semibold',
                      isActive ? 'text-primary' : 'text-foreground',
                    )}
                    numberOfLines={2}
                  >
                    {titleText}
                  </Text>
                  <Text className="text-xs text-muted-foreground mt-1.5">
                    {episode.duration ? formatDuration(episode.duration) : ''}
                  </Text>
                </View>
                <View className="ml-3">
                  <View
                    className={cn(
                      'w-11 h-11 rounded-full items-center justify-center',
                      isActive ? 'bg-primary' : 'bg-muted',
                    )}
                  >
                    {isActive && playbackState === PlaybackState.Buffering ? (
                      <ActivityIndicator
                        size="small"
                        color={colors.onPrimary}
                      />
                    ) : (
                      <Ionicons
                        name={isActive && isPlaying ? 'pause' : 'play'}
                        size={22}
                        color={isActive ? colors.onPrimary : colors.muted}
                      />
                    )}
                  </View>
                </View>
              </View>
            </Pressable>
          );
        })}
      </View>
    </ScrollView>
  );
}
