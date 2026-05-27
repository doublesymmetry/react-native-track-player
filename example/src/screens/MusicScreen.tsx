import { useCallback, useMemo } from 'react';
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
import { buildAlbum } from '../data/music';
import { useDownloadedTrackStore } from '../stores/downloadedTrack';
import { formatDuration } from '../lib/formatTime';
import { cn } from '../lib/cn';

const buttonIconColors = {
  light: {
    onPrimary: '#fff',
    onSecondary: '#0f0f0f',
    primary: 'hsl(152, 60%, 42%)',
  },
  dark: {
    onPrimary: '#fff',
    onSecondary: '#f5f5f5',
    primary: 'hsl(152, 60%, 42%)',
  },
} as const;

export default function MusicScreen() {
  const insets = useSafeAreaInsets();
  const colorScheme = useColorScheme() ?? 'light';
  const colors = buttonIconColors[colorScheme];
  const activeMediaItem = useActiveMediaItem();
  const isPlaying = useIsPlaying();
  const playbackState = usePlaybackState();

  const setSource = usePlaybackSourceStore(s => s.setSource);
  const downloadedFileUri = useDownloadedTrackStore(s => s.fileUri);
  const album = useMemo(
    () => buildAlbum(downloadedFileUri),
    [downloadedFileUri],
  );

  const playAlbum = useCallback(
    (startIndex = 0) => {
      TrackPlayer.setPlaybackSpeed(1);
      setSource('music');
      TrackPlayer.setCommands({
        capabilities: [
          PlayerCommand.PlayPause,
          PlayerCommand.Next,
          PlayerCommand.Previous,
          PlayerCommand.Seek,
        ],
      });
      TrackPlayer.setMediaItems(album.tracks, startIndex);
      TrackPlayer.play();
    },
    [setSource, album.tracks],
  );

  const shuffleAlbum = useCallback(() => {
    TrackPlayer.setPlaybackSpeed(1);
    setSource('music');
    TrackPlayer.setCommands({
      capabilities: [
        PlayerCommand.PlayPause,
        PlayerCommand.Next,
        PlayerCommand.Previous,
        PlayerCommand.Seek,
      ],
    });
    TrackPlayer.setMediaItems(album.tracks);
    TrackPlayer.setShuffleEnabled(true);
    TrackPlayer.play();
  }, [setSource, album.tracks]);

  const contentContainerStyle = {
    paddingTop: insets.top + 16,
    paddingBottom: 32,
  };

  return (
    <ScrollView
      className="flex-1 bg-background"
      contentContainerStyle={contentContainerStyle}
    >
      {/* Album Header */}
      <View className="items-center px-8 pb-6">
        <View className="shadow-xl">
          <Image
            source={{ uri: album.artwork }}
            className="w-60 h-60 rounded-xl"
          />
        </View>
        <Text className="text-2xl font-bold text-foreground mt-5">
          {album.title}
        </Text>
        <Text className="text-sm text-primary font-medium mt-1">
          {album.artist}
        </Text>
        <Text className="text-xs text-muted-foreground mt-0.5">
          {album.year} · {album.tracks.length} songs
        </Text>
      </View>

      {/* Action Buttons */}
      <View className="flex-row justify-center gap-3 px-8 mb-4">
        <TouchableOpacity
          className="flex-1 h-12 rounded-full bg-primary flex-row items-center justify-center gap-2"
          onPress={() => playAlbum(0)}
          activeOpacity={0.8}
        >
          <Ionicons name="play" size={20} color={colors.onPrimary} />
          <Text className="text-primary-foreground font-bold text-sm">
            Play
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          className="flex-1 h-12 rounded-full bg-secondary flex-row items-center justify-center gap-2"
          onPress={shuffleAlbum}
          activeOpacity={0.8}
        >
          <Ionicons name="shuffle" size={20} color={colors.onSecondary} />
          <Text className="text-secondary-foreground font-bold text-sm">
            Shuffle
          </Text>
        </TouchableOpacity>
      </View>

      {/* Track List */}
      <View className="px-4 mt-2">
        {album.tracks.map((track, index) => {
          const isActive = activeMediaItem?.mediaId === track.mediaId;
          return (
            <Pressable
              key={track.mediaId}
              className={cn(
                'flex-row items-center py-3.5 px-3 rounded-[10px]',
                isActive ? 'bg-primary/5' : '',
              )}
              onPress={() => {
                if (isActive && isPlaying) {
                  TrackPlayer.pause();
                } else if (isActive) {
                  TrackPlayer.play();
                } else {
                  playAlbum(index);
                }
              }}
            >
              <View className="w-7 items-center justify-center">
                {isActive && playbackState === PlaybackState.Buffering ? (
                  <ActivityIndicator size="small" color={colors.primary} />
                ) : (
                  <Text
                    className={cn(
                      'text-sm text-center',
                      isActive
                        ? 'text-primary font-bold'
                        : 'text-muted-foreground',
                    )}
                  >
                    {isActive && isPlaying ? '♫' : `${index + 1}`}
                  </Text>
                )}
              </View>
              <View className="flex-1 ml-3">
                <View className="flex-row items-center gap-2 flex-wrap">
                  <Text
                    className={cn(
                      'text-base',
                      isActive
                        ? 'text-primary font-semibold'
                        : 'text-foreground',
                    )}
                    numberOfLines={1}
                  >
                    {track.title}
                  </Text>
                  {track.isLocal && (
                    <View className="bg-primary/15 px-2 py-0.5 rounded">
                      <Text className="text-[10px] font-semibold text-primary">
                        Local
                      </Text>
                    </View>
                  )}
                </View>
                <Text className="text-xs text-muted-foreground mt-0.5">
                  {track.artist}
                </Text>
              </View>
              <Text className="text-xs text-muted-foreground ml-2">
                {track.duration ? formatDuration(track.duration) : ''}
              </Text>
            </Pressable>
          );
        })}
      </View>
    </ScrollView>
  );
}
