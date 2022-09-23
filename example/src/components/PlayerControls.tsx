import React from 'react';
import { StyleSheet, View } from 'react-native';
import TrackPlayer from 'react-native-track-player';

import { Button } from './Button';
import { PlaybackError } from './PlaybackError';
import { PlayPauseButton } from './PlayPauseButton';

export const PlayerControls: React.FC<{ error?: string }> = ({ error }) => {
  return (
    <View style={styles.container}>
      <View style={styles.row}>
        <Button
          title="Prev"
          onPress={() => TrackPlayer.skipToPrevious()}
          type="secondary"
        />
        <PlayPauseButton error={error} />
        <Button
          title="Next"
          onPress={() => TrackPlayer.skipToNext()}
          type="secondary"
        />
      </View>
      <PlaybackError error={error} />
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
