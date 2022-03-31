import React, {useCallback, useEffect, useState} from 'react';
import {
  ActivityIndicator,
  Image,
  SafeAreaView,
  StatusBar,
  StyleSheet,
  Text,
  TouchableWithoutFeedback,
  View,
} from 'react-native';
import Slider from '@react-native-community/slider';
import TrackPlayer, {
  Capability,
  Event,
  RepeatMode,
  State,
  Track,
  usePlaybackState,
  useProgress,
  useTrackPlayerEvents,
} from 'react-native-track-player';

// @ts-ignore
import playlistData from './react/data/playlist.json';
// @ts-ignore
import localTrack from './react/resources/pure.m4a';
// @ts-ignore
import localArtwork from './react/resources/artwork.jpg';

const setupPlayer = async () => {
  let index = 0;
  try {
    // this method will only reject if player has not been setup yet
    index = await TrackPlayer.getCurrentTrack();
  } catch {
    await TrackPlayer.setupPlayer();
    await TrackPlayer.updateOptions({
      stopWithApp: false,
      capabilities: [
        Capability.Play,
        Capability.Pause,
        Capability.SkipToNext,
        Capability.SkipToPrevious,
        Capability.Stop,
      ],
      compactCapabilities: [
        Capability.Play,
        Capability.Pause,
        Capability.SkipToNext,
      ],
    });
    await TrackPlayer.add([
      ...playlistData,
      {
        url: localTrack,
        title: 'Pure (Demo)',
        artist: 'David Chavez',
        artwork: localArtwork,
        duration: 28,
      },
    ]);
    await TrackPlayer.setRepeatMode(RepeatMode.Queue);
  } finally {
    return index;
  }
};

const App = () => {
  const progress = useProgress();
  const state = usePlaybackState();
  const [index, setIndex] = useState<number | undefined>();
  const [track, setTrack] = useState<Track | undefined>();
  const isPlaying = state === State.Playing;
  const isLoading = state === State.Connecting || state === State.Buffering;

  useEffect(() => {
    let mounted = true;
    (async () => {
      const trackIndex = await setupPlayer();
      if (mounted) {
        setIndex(trackIndex);
      }
    })();
    return () => {
      mounted = false;
    };
  }, []);

  useTrackPlayerEvents(
    [Event.PlaybackTrackChanged],
    async ({ nextTrack }) => {
      setIndex(nextTrack);
    },
  );

  useEffect(() => {
    if (index === undefined) return;
    let mounted = true;
    (async () => {
      const track = await TrackPlayer.getTrack(index);
      if (mounted) setTrack(track);
    })();
    return () => {
      mounted = false;
    };
  }, [index]);

  const performTogglePlayback = useCallback(() => {
    if (index === undefined) return;
    if (isPlaying) {
      TrackPlayer.pause();
    } else {
      TrackPlayer.play();
    }
  }, [isPlaying]);

  return (
    <SafeAreaView style={styles.screenContainer}>
      <StatusBar barStyle={'light-content'} />
      <View style={styles.contentContainer}>
        <View style={styles.topBarContainer}>
          <TouchableWithoutFeedback>
            <Text style={styles.queueButton}>Queue</Text>
          </TouchableWithoutFeedback>
        </View>
        <Image style={styles.artwork} source={{uri: `${track?.artwork}`}} />
        <Text style={styles.titleText}>{track?.title}</Text>
        <Text style={styles.artistText}>{track?.artist}</Text>
        <Slider
          style={styles.progressContainer}
          value={progress.position}
          minimumValue={0}
          maximumValue={progress.duration}
          thumbTintColor="#FFD479"
          minimumTrackTintColor="#FFD479"
          maximumTrackTintColor="#FFFFFF"
          onSlidingComplete={value => {
            TrackPlayer.seekTo(value);
          }}
        />
        <View style={styles.progressLabelContainer}>
          <Text style={styles.progressLabelText}>
            {new Date(progress.position * 1000).toISOString().slice(14, 19)}
          </Text>
          <Text style={styles.progressLabelText}>
            {new Date((progress.duration - progress.position) * 1000)
              .toISOString()
              .slice(14, 19)}
          </Text>
        </View>
      </View>
      <View style={styles.actionRowContainer}>
        <TouchableWithoutFeedback onPress={() => TrackPlayer.skipToPrevious()}>
          <Text style={styles.secondaryActionButton}>Prev</Text>
        </TouchableWithoutFeedback>
        <TouchableWithoutFeedback onPress={performTogglePlayback}>
          <Text style={styles.primaryActionButton}>
            {isPlaying ? 'Pause' : 'Play'}
          </Text>
        </TouchableWithoutFeedback>
        <TouchableWithoutFeedback onPress={() => TrackPlayer.skipToNext()}>
          <Text style={styles.secondaryActionButton}>Next</Text>
        </TouchableWithoutFeedback>
      </View>
      <View style={styles.statusContainer}>
        {isLoading && <ActivityIndicator />}
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  screenContainer: {
    flex: 1,
    backgroundColor: '#212121',
    alignItems: 'center',
  },
  contentContainer: {
    flex: 1,
    alignItems: 'center',
  },
  topBarContainer: {
    width: '100%',
    flexDirection: 'row',
    paddingHorizontal: 20,
    justifyContent: 'flex-end',
  },
  queueButton: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#FFD479',
  },
  artwork: {
    width: 240,
    height: 240,
    marginTop: 30,
    backgroundColor: 'grey',
  },
  titleText: {
    fontSize: 18,
    fontWeight: '600',
    color: 'white',
    marginTop: 30,
  },
  artistText: {
    fontSize: 16,
    fontWeight: '200',
    color: 'white',
  },
  progressContainer: {
    height: 40,
    width: 380,
    marginTop: 25,
    flexDirection: 'row',
  },
  progressLabelContainer: {
    width: 370,
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  progressLabelText: {
    color: 'white',
    fontVariant: ['tabular-nums'],
  },
  actionRowContainer: {
    width: '60%',
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  primaryActionButton: {
    fontSize: 18,
    fontWeight: '600',
    color: '#FFD479',
  },
  secondaryActionButton: {
    fontSize: 14,
    color: '#FFD479',
  },
  statusContainer: {
    height: 40,
    marginTop: 20,
    marginBottom: 60,
  },
});

export default App;
