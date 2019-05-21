import React from 'react';
import PropTypes from 'prop-types';
import {
  Text,
  TouchableOpacity,
  StyleSheet,
  View
} from 'react-native';
import TrackPlayer from 'react-native-track-player';
import { ControlButton } from './ControlButton';
import { useNextTrack, usePreviousTrack, usePlaybackStateIs } from '../hooks';

const skipToNext = () => {
  TrackPlayer.skipToNext();
};

const skipToPrevious = () => {
  TrackPlayer.skipToPrevious();
};

export function Controls() {
  const nextTrack = useNextTrack();
  const previousTrack = usePreviousTrack();
  const isPlaying = usePlaybackStateIs(TrackPlayer.STATE_PLAYING);
  const togglePlayback = () => {
    if (isPlaying) {
      TrackPlayer.pause();
    } else {
      TrackPlayer.play();
    }
  }
  return (
    <View style={styles.controls}>
      <ControlButton
        title={'<<'}
        active={!!previousTrack}
        onPress={previousTrack ? skipToPrevious : null}
      />
      <ControlButton
        title={isPlaying ? 'Pause' : 'Play'}
        onPress={togglePlayback}
      />
      <ControlButton
        title={'>>'}
        active={!!nextTrack}
        onPress={nextTrack ? skipToNext : null}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1
  },
  text: {
    fontSize: 18,
    textAlign: 'center'
  },
  inactive: {
    opacity: 0.5
  },
  controls: {
    marginVertical: 20,
    flexDirection: 'row'
  }
});
