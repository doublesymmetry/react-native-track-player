import PropTypes from 'prop-types';
import React from 'react';
import TrackPlayer, { useTrackPlayerEvents } from 'react-native-track-player';
import {
  Image,
  StyleSheet,
  Text,
  View
} from 'react-native';
import { ProgressBar } from './ProgressBar';
import { Controls } from './Controls';

export default function Player({ tracks, style }) {
  const [track, setTrack] = useState(tracks[0]);

  useEffect(async () => {
    TrackPlayer.setupPlayer();
    TrackPlayer.updateOptions({
      stopWithApp: true,
      capabilities: [
        TrackPlayer.CAPABILITY_PLAY,
        TrackPlayer.CAPABILITY_PAUSE,
        TrackPlayer.CAPABILITY_SKIP_TO_NEXT,
        TrackPlayer.CAPABILITY_SKIP_TO_PREVIOUS,
        TrackPlayer.CAPABILITY_STOP
      ],
      compactCapabilities: [
        TrackPlayer.CAPABILITY_PLAY,
        TrackPlayer.CAPABILITY_PAUSE
      ]
    });
  }, []);

  useTrackPlayerEvents([PLAYBACK_TRACK_CHANGED], ({ nextTrack }) => {
    setTrack(tracks.find(({ id }) => id === nextTrack));
  });

  useEffect(async () => {
    await TrackPlayer.reset();
    await TrackPlayer.add(tracks);
  }, [tracks]);

  return (
    <>
      <View style={[styles.card, style]}>
        <Image style={styles.cover} source={{ uri: track.artwork }} />
        <ProgressBar />
        <Text style={styles.title}>{track.title}</Text>
        <Text style={styles.artist}>{track.artist}</Text>
        <Controls />
      </View>
    </>
  );
}

const styles = StyleSheet.create({
  card: {
    width: '80%',
    elevation: 1,
    borderRadius: 4,
    shadowRadius: 2,
    shadowOpacity: 0.1,
    alignItems: 'center',
    shadowColor: 'black',
    backgroundColor: 'white',
    shadowOffset: { width: 0, height: 1 }
  },
  cover: {
    width: 140,
    height: 140,
    marginTop: 20,
    backgroundColor: 'grey'
  },
  title: {
    marginTop: 10
  },
  artist: {
    fontWeight: 'bold'
  },
  controls: {
    marginVertical: 20,
    flexDirection: 'row'
  }
});
