import React from 'react';
import {ActivityIndicator, View, StyleSheet} from 'react-native';
import {usePlaybackState, State} from 'react-native-track-player';

import {Button} from './Button';
import {useOnTogglePlayback} from '../hooks';

export const PlayPauseButton: React.FC = () => {
  const state = usePlaybackState();
  const isPlaying = state === State.Playing;
  const isLoading = state === State.Connecting || state === State.Buffering;

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
    />
  );
};

const styles = StyleSheet.create({
  statusContainer: {
    height: 40,
    marginTop: 20,
    marginBottom: 60,
  },
});
