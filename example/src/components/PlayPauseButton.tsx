import React from 'react';
import { ActivityIndicator, StyleSheet, View } from 'react-native';
import TrackPlayer, { useIsPlaying } from 'react-native-track-player';
import { Button } from './Button';

export const PlayPauseButton: React.FC = () => {
  const { playing, bufferingDuringPlay } = useIsPlaying();

  return bufferingDuringPlay ? (
    <View style={styles.statusContainer}>
      <ActivityIndicator />
    </View>
  ) : (
    <Button
      title={playing ? 'Pause' : 'Play'}
      onPress={playing ? TrackPlayer.pause : TrackPlayer.play}
      type="primary"
      style={styles.playPause}
    />
  );
};

const styles = StyleSheet.create({
  playPause: {
    width: 120,
    textAlign: 'center',
  },
  statusContainer: {
    height: 40,
    width: 120,
    marginTop: 20,
    marginBottom: 60,
  },
});
