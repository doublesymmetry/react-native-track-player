import React from 'react';
import { ActivityIndicator, StyleSheet, View } from 'react-native';
import { State, usePlaybackState } from 'react-native-track-player';
import { useOnTogglePlayback } from '../hooks';
import { useDebouncedValue } from '../hooks/useDebouncedValue';

import { Button } from './Button';

export const PlayPauseButton: React.FC = () => {
  const state = usePlaybackState();
  const isPlaying = state === State.Playing;
  const isLoading = useDebouncedValue(
    state === State.Connecting || state === State.Buffering,
    250
  );

  const onTogglePlayback = useOnTogglePlayback();

  if (isLoading) {
    return (
      <View style={styles.statusContainer}>
        {isLoading && <ActivityIndicator />}
      </View>
    );
  }

  return (
    <Button
      title={isPlaying ? 'Pause' : 'Play'}
      onPress={onTogglePlayback}
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
