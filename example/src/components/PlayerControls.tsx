import React from 'react';
import {View, StyleSheet} from 'react-native';
import TrackPlayer from 'react-native-track-player';

import {Button} from './Button';
import {PlayPauseButton} from './PlayPauseButton';

export const PlayerControls: React.FC = () => {
  return (
    <View style={{ width: '100%' }}>
      <View style={styles.row}>
        <Button
          title="Prev"
          onPress={() => TrackPlayer.skipToPrevious()}
          type="secondary"
        />
        <PlayPauseButton />
        <Button
          title="Next"
          onPress={() => TrackPlayer.skipToNext()}
          type="secondary"
        />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    width: '100%',
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-evenly'
  },
});
