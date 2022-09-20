import React from 'react';
import { ActivityIndicator, StyleSheet, View } from 'react-native';
import TrackPlayer, {
  State,
  usePlaybackState,
  usePlayWhenReady,
} from 'react-native-track-player';
import { useDebouncedValue } from '../hooks';
import { Button } from './Button';

export const PlayPauseButton: React.FC = () => {
  const state = usePlaybackState();
  const playWhenReady = usePlayWhenReady();
  const isLoading = useDebouncedValue(
    state === State.Connecting || state === State.Buffering,
    250
  );

  const showLoadingIndicator = playWhenReady && isLoading;

  if (isLoading) {
    return (
      <View style={styles.statusContainer}>
        {showLoadingIndicator && <ActivityIndicator />}
      </View>
    );
  }

  return (
    <Button
      title={playWhenReady ? 'Pause' : 'Play'}
      onPress={playWhenReady ? TrackPlayer.pause : TrackPlayer.play}
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
