import React from 'react';
import { StyleSheet, View } from 'react-native';
import TrackPlayer, { usePlaybackState } from 'react-native-track-player';

import { Button } from './Button';
import { PlaybackError } from './PlaybackError';
import { PlayPauseButton } from './PlayPauseButton';

export const PlayerControls: React.FC = () => {
  const playbackState = usePlaybackState();
  return (
    <View style={styles.container}>
      <View style={styles.row}>
        <Button
          title="Prev"
          onPress={() => TrackPlayer.skipToPrevious()}
          type="secondary"
        />
        <PlayPauseButton playbackState={playbackState} />
        <Button
          title="Next"
          onPress={() => TrackPlayer.skipToNext()}
          type="secondary"
        />
      </View>
      <PlaybackError playbackState={playbackState} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    width: '100%',
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-evenly',
  },
});
