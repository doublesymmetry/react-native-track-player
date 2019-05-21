import React, { useState } from 'react';
import {
  Image,
  StyleSheet,
  Text,
  View
} from 'react-native';
import { useTrackPlayerProgress } from 'react-native-track-player';

export function ProgressBar() {
  const { position, bufferedPosition, duration } = useTrackPlayerProgress(100);
  const progress = duration
    ? position / duration
    : 0;
  const bufferedProgress = duration
    ? bufferedPosition / duration
    : 0;
  return (
    <View style={styles.progress}>
      <View style={{ flex: progress, backgroundColor: 'red' }} />
      <View style={{ flex: bufferedProgress - progress, backgroundColor: 'grey' }} />
      <View style={{ flex: 1 - bufferedProgress, backgroundColor: 'lightgray' }} />
    </View>
  );
}

const styles = StyleSheet.create({
  progress: {
    height: 1,
    width: '90%',
    marginTop: 10,
    flexDirection: 'row'
  }
});
