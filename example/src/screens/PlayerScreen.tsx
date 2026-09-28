import { useCallback, useEffect, useState } from 'react';
import {
  View,
  Text,
  Image,
  TouchableOpacity,
  Pressable,
  Platform,
  useWindowDimensions,
  Animated,
  useColorScheme,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import Ionicons from 'react-native-vector-icons/Ionicons';
import TrackPlayer, {
  OutputDeviceButton,
  useActiveMediaItem,
  useIsPlaying,
  useProgress,
  Event,
} from '@rntp/player';
import { RepeatMode } from '@rntp/player';
import { usePlaybackSourceStore } from '../stores/playbackSource';
import { LOCAL_MEDIA_IDS } from '../data/music';
import { formatTime } from '../lib/formatTime';
import type { RootStackParamList } from '../navigation/types';

const SPEED_OPTIONS = [0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0];

type Props = NativeStackScreenProps<RootStackParamList, 'Player'>;

const iconColors = {
  light: {
    foreground: '#0f0f0f',
    muted: '#737373',
    primary: 'hsl(152, 60%, 42%)',
  },
  dark: {
    foreground: '#f5f5f5',
    muted: '#999999',
    primary: 'hsl(152, 60%, 42%)',
  },
} as const;

const playIconStartMargin = { marginLeft: 3 };

export default function PlayerScreen({ navigation }: Props) {
  const insets = useSafeAreaInsets();
  const { width } = useWindowDimensions();
  const colorScheme = useColorScheme() === 'dark' ? 'dark' : 'light';
  const colors = iconColors[colorScheme];
  const activeMediaItem = useActiveMediaItem();
  const isPlaying = useIsPlaying();
  const { position, duration, cached } = useProgress(0.5);
  const playbackSource = usePlaybackSourceStore(s => s.source);

  const [repeatMode, setRepeatMode] = useState<RepeatMode>(
    TrackPlayer.getRepeatMode(),
  );
  const [shuffleEnabled, setShuffleEnabled] = useState(
    TrackPlayer.isShuffleEnabled(),
  );
  const [speed, setSpeed] = useState(1.0);
  const [sleepTimer, setSleepTimer] = useState<{
    type: 'time' | 'mediaItem';
    label: string;
  } | null>(null);

  useEffect(() => {
    setRepeatMode(TrackPlayer.getRepeatMode());
    setShuffleEnabled(TrackPlayer.isShuffleEnabled());
  }, []);

  // Sync sleep timer state & listen for trigger
  useEffect(() => {
    const timer = TrackPlayer.getSleepTimer();
    if (timer) {
      setSleepTimer({
        type: timer.type,
        label:
          timer.type === 'time'
            ? `${Math.ceil(timer.remainingSeconds / 60)}m`
            : 'Track',
      });
    }
    const sub = TrackPlayer.addEventListener(Event.SleepTimerTriggered, () => {
      setSleepTimer(null);
    });
    return () => sub.remove();
  }, []);

  const startSleepTimer = useCallback((minutes: number) => {
    TrackPlayer.sleepAfterTime(minutes * 60, { fadeOutSeconds: 30 });
    setSleepTimer({ type: 'time', label: `${minutes}m` });
  }, []);

  const sleepAfterTrack = useCallback(() => {
    TrackPlayer.sleepAfterMediaItemAtIndex();
    setSleepTimer({ type: 'mediaItem', label: 'Track' });
  }, []);

  const cancelSleep = useCallback(() => {
    TrackPlayer.cancelSleepTimer();
    setSleepTimer(null);
  }, []);

  useEffect(() => {
    if (playbackSource !== 'podcast') {
      TrackPlayer.setPlaybackSpeed(1);
      setSpeed(1);
    } else {
      setSpeed(TrackPlayer.getPlaybackSpeed());
    }
  }, [playbackSource]);

  const isLive = activeMediaItem?.isLive ?? false;
  const isLocal = activeMediaItem?.mediaId
    ? LOCAL_MEDIA_IDS.has(activeMediaItem.mediaId)
    : false;
  const isPodcast = playbackSource === 'podcast';
  const artworkSize = Math.min(width - 64, 340);

  const artworkUrl =
    typeof activeMediaItem?.artworkUrl === 'string'
      ? activeMediaItem.artworkUrl
      : undefined;

  const cycleRepeat = useCallback(() => {
    const modes = [RepeatMode.Off, RepeatMode.All, RepeatMode.One] as const;
    const currentIdx = modes.indexOf(repeatMode);
    const next = modes[(currentIdx + 1) % modes.length]!;
    setRepeatMode(next);
    TrackPlayer.setRepeatMode(next);
  }, [repeatMode]);

  const toggleShuffle = useCallback(() => {
    const next = !shuffleEnabled;
    setShuffleEnabled(next);
    TrackPlayer.setShuffleEnabled(next);
  }, [shuffleEnabled]);

  if (!activeMediaItem) {
    navigation.goBack();
    return null;
  }

  const headerTopPadding =
    Platform.OS === 'android'
      ? insets.top + 12
      : Math.max(12, insets.top * 0.4);

  return (
    <View
      className="flex-1 bg-background"
      style={{
        paddingTop: headerTopPadding,
        paddingBottom: insets.bottom,
      }}
    >
      {/* Header: drag indicator (iOS) or dismiss chevron (Android) */}
      <View className="flex-row items-center justify-between px-2 pb-2">
        {Platform.OS === 'android' ? (
          <TouchableOpacity
            className="w-10 h-10 items-center justify-center ml-2"
            onPress={() => navigation.goBack()}
            activeOpacity={0.7}
          >
            <Ionicons name="chevron-down" size={28} color={colors.foreground} />
          </TouchableOpacity>
        ) : (
          <View className="w-9" />
        )}
        {Platform.OS === 'ios' ? (
          <View className="w-9 h-[5px] rounded-[3px] bg-muted" />
        ) : (
          <View className="flex-1" />
        )}
        <View className="w-9" />
      </View>

      {/* Artwork */}
      <View className="items-center flex-1 justify-center px-8">
        <View className="shadow-2xl">
          {artworkUrl ? (
            <Image
              source={{ uri: artworkUrl }}
              className="rounded-[20px]"
              style={{
                width: artworkSize,
                height: artworkSize,
              }}
            />
          ) : (
            <View
              style={{
                width: artworkSize,
                height: artworkSize,
              }}
              className="rounded-[20px] bg-secondary items-center justify-center"
            >
              <Ionicons name="musical-notes" size={80} color={colors.muted} />
            </View>
          )}
        </View>
      </View>

      {/* Track Info + Controls Section */}
      <View className="px-8">
        {/* Track Info */}
        <View className="mb-4">
          <View className="flex-row items-center gap-2 flex-wrap">
            <Text
              className="text-xl font-bold text-foreground"
              numberOfLines={1}
            >
              {activeMediaItem.title ?? 'Unknown'}
            </Text>
            {isLocal && (
              <View className="bg-primary/15 px-2.5 py-1 rounded">
                <Text className="text-xs font-semibold text-primary">
                  Local
                </Text>
              </View>
            )}
          </View>
          <Text
            className="text-base text-muted-foreground mt-1"
            numberOfLines={1}
          >
            {activeMediaItem.artist ?? ''}
          </Text>
        </View>

        {/* Progress / Live indicator */}
        {!isLive ? (
          <View className="mb-5">
            <SeekBar position={position} duration={duration} cached={cached} />
            <View className="flex-row justify-between mt-2">
              <Text className="text-xs text-muted-foreground">
                {formatTime(position)}
              </Text>
              <Text className="text-xs text-muted-foreground">
                {duration > 0 ? `-${formatTime(duration - position)}` : '--:--'}
              </Text>
            </View>
          </View>
        ) : (
          <View className="mb-5 items-center">
            <View className="flex-row items-center bg-red-500/10 px-4 py-2 rounded-[20px]">
              <PulsingDot />
              <Text className="font-semibold ml-2 text-red-500 text-[13px]">
                Live Stream
              </Text>
            </View>
          </View>
        )}

        {/* Transport Controls */}
        <View className="flex-row items-center justify-between mb-3">
          {/* Shuffle / Spacer */}
          <TouchableOpacity
            className="w-11 h-11 items-center justify-center"
            onPress={isLive ? undefined : toggleShuffle}
            activeOpacity={isLive ? 1 : 0.6}
          >
            {!isLive && (
              <Ionicons
                name="shuffle"
                size={22}
                color={shuffleEnabled ? colors.primary : colors.muted}
              />
            )}
          </TouchableOpacity>

          {/* Previous */}
          <TouchableOpacity
            className="w-[52px] h-[52px] items-center justify-center"
            onPress={() => {
              if (isLive) {
                TrackPlayer.seekBy(-15);
              } else {
                TrackPlayer.skipToPrevious();
              }
            }}
            activeOpacity={0.6}
          >
            <Ionicons
              name={isLive ? 'play-back' : 'play-skip-back'}
              size={28}
              color={colors.foreground}
            />
          </TouchableOpacity>

          {/* Play/Pause */}
          <TouchableOpacity
            className="w-[68px] h-[68px] rounded-full bg-foreground items-center justify-center"
            onPress={() => {
              if (isPlaying) TrackPlayer.pause();
              else TrackPlayer.play();
            }}
            activeOpacity={0.8}
          >
            <Ionicons
              name={isPlaying ? 'pause' : 'play'}
              size={32}
              color="#fff"
              style={isPlaying ? undefined : playIconStartMargin}
            />
          </TouchableOpacity>

          {/* Next / Forward */}
          <TouchableOpacity
            className="w-[52px] h-[52px] items-center justify-center"
            onPress={() => {
              if (isLive) {
                TrackPlayer.seekToLiveEdge();
              } else {
                TrackPlayer.skipToNext();
              }
            }}
            activeOpacity={0.6}
          >
            <Ionicons
              name={isLive ? 'play-forward' : 'play-skip-forward'}
              size={28}
              color={colors.foreground}
            />
          </TouchableOpacity>

          {/* Repeat / Spacer */}
          <TouchableOpacity
            className="w-11 h-11 items-center justify-center"
            onPress={isLive ? undefined : cycleRepeat}
            activeOpacity={isLive ? 1 : 0.6}
          >
            {!isLive && (
              <View className="relative items-center justify-center">
                <Ionicons
                  name="repeat"
                  size={22}
                  color={
                    repeatMode !== RepeatMode.Off
                      ? colors.primary
                      : colors.muted
                  }
                />
                {repeatMode === RepeatMode.One && (
                  <Text className="absolute text-[8px] font-bold text-primary -bottom-0.5">
                    1
                  </Text>
                )}
              </View>
            )}
          </TouchableOpacity>
        </View>

        {/* Skip ±15s: only for non-live (music/podcast) */}
        {!isLive && (
          <View className="flex-row items-center justify-center gap-4 mb-4">
            <TouchableOpacity
              className="bg-secondary items-center justify-center px-4 h-8 rounded-2xl"
              onPress={() => TrackPlayer.seekBy(-15)}
              activeOpacity={0.7}
            >
              <Text className="text-secondary-foreground text-xs font-semibold">
                −15s
              </Text>
            </TouchableOpacity>
            <TouchableOpacity
              className="bg-secondary items-center justify-center px-4 h-8 rounded-2xl"
              onPress={() => TrackPlayer.seekBy(15)}
              activeOpacity={0.7}
            >
              <Text className="text-secondary-foreground text-xs font-semibold">
                +15s
              </Text>
            </TouchableOpacity>
          </View>
        )}

        {/* Output device picker (AirPlay on iOS, Cast on Android) */}
        <View className="items-center mb-2">
          <OutputDeviceButton style={{ width: 36, height: 36 }} />
        </View>

        {/* Sleep Timer */}
        <View className="flex-row items-center justify-center gap-2 mb-4">
          <Ionicons name="moon-outline" size={14} color={colors.muted} />
          {sleepTimer ? (
            <>
              <Text className="text-xs text-primary font-semibold">
                {sleepTimer.label}
              </Text>
              <TouchableOpacity
                className="bg-secondary items-center justify-center px-3.5 h-7 rounded-[14px]"
                onPress={cancelSleep}
                activeOpacity={0.7}
              >
                <Text className="text-secondary-foreground text-xs font-semibold">
                  Cancel
                </Text>
              </TouchableOpacity>
            </>
          ) : (
            <>
              {[5, 15, 30].map(m => (
                <TouchableOpacity
                  key={m}
                  className="bg-secondary items-center justify-center px-3 h-7 rounded-[14px]"
                  onPress={() => startSleepTimer(m)}
                  activeOpacity={0.7}
                >
                  <Text className="text-secondary-foreground text-xs font-semibold">
                    {m}m
                  </Text>
                </TouchableOpacity>
              ))}
              {!isLive && (
                <TouchableOpacity
                  className="bg-secondary items-center justify-center px-3 h-7 rounded-[14px]"
                  onPress={sleepAfterTrack}
                  activeOpacity={0.7}
                >
                  <Text className="text-secondary-foreground text-xs font-semibold">
                    Track
                  </Text>
                </TouchableOpacity>
              )}
            </>
          )}
        </View>

        {/* Playback speed: only for podcast */}
        {isPodcast && (
          <View className="flex-row items-center justify-center gap-2 mb-4">
            <Text className="text-xs text-muted-foreground font-medium">
              Speed
            </Text>
            <TouchableOpacity
              className="bg-secondary items-center justify-center px-3.5 h-7 rounded-[14px]"
              onPress={() => {
                const idx = SPEED_OPTIONS.indexOf(speed);
                const next =
                  SPEED_OPTIONS[(idx + 1) % SPEED_OPTIONS.length] ?? 1;
                setSpeed(next);
                TrackPlayer.setPlaybackSpeed(next);
              }}
              activeOpacity={0.7}
            >
              <Text className="text-secondary-foreground text-xs font-semibold">
                {`${speed}x`}
              </Text>
            </TouchableOpacity>
          </View>
        )}
      </View>
    </View>
  );
}

function SeekBar({
  position,
  duration,
  cached,
}: {
  position: number;
  duration: number;
  cached: number;
}) {
  const progress = duration > 0 ? Math.min(position / duration, 1) : 0;
  const cachedProgress = duration > 0 ? Math.min(cached / duration, 1) : 0;
  const [barWidth, setBarWidth] = useState(0);

  return (
    <Pressable
      className="h-6 justify-center"
      onLayout={e => setBarWidth(e.nativeEvent.layout.width)}
      onPress={e => {
        if (barWidth > 0 && duration > 0) {
          const seekTo = (e.nativeEvent.locationX / barWidth) * duration;
          TrackPlayer.seekTo(seekTo);
        }
      }}
    >
      <View className="h-1 rounded bg-muted overflow-hidden">
        {cachedProgress > 0 && (
          <View
            className="absolute h-full rounded bg-foreground/20"
            style={{ width: `${cachedProgress * 100}%` }}
          />
        )}
        <View
          className="h-full rounded bg-foreground"
          style={{ width: `${progress * 100}%` }}
        />
      </View>
      <View
        className="absolute w-3.5 h-3.5 rounded-full bg-foreground -ml-[7px]"
        style={{ left: `${progress * 100}%` }}
      />
    </Pressable>
  );
}

function PulsingDot() {
  const [anim] = useState(() => new Animated.Value(1));

  useEffect(() => {
    const pulse = Animated.loop(
      Animated.sequence([
        Animated.timing(anim, {
          toValue: 0.3,
          duration: 800,
          useNativeDriver: true,
        }),
        Animated.timing(anim, {
          toValue: 1,
          duration: 800,
          useNativeDriver: true,
        }),
      ]),
    );
    pulse.start();
    return () => pulse.stop();
  }, [anim]);

  return (
    <Animated.View
      className="w-2 h-2 rounded-full bg-red-500"
      style={{ opacity: anim }}
    />
  );
}
