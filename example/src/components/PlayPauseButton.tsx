import React from 'react';
import { ActivityIndicator, StyleSheet, View } from 'react-native';
import TrackPlayer, {
  State,
  usePlayWhenReady,
} from 'react-native-track-player';
import { useDebouncedValue } from '../hooks';
import { Button } from './Button';

export const PlayPauseButton: React.FC<{
  state: State | undefined;
}> = ({ state }) => {
  const playWhenReady = usePlayWhenReady();
  const isLoading = useDebouncedValue(
    state === State.Loading || state === State.Buffering,
    250
  );

  const isErrored = state === State.Error;
  const isEnded = state === State.Ended;
  const showPause = playWhenReady && !(isErrored || isEnded);
  const showBuffering = playWhenReady && isLoading;
  return showBuffering ? (
    <View style={styles.statusContainer}>
      <ActivityIndicator />
    </View>
  ) : (
    <Button
      title={showPause ? 'Pause' : 'Play'}
      onPress={showPause ? TrackPlayer.pause : TrackPlayer.play}
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
