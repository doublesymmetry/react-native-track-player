import React, { useState } from 'react';
import {
  Image,
  StyleSheet,
  Text,
  View
} from 'react-native';
import { useTrackPlayerProgress } from 'react-native-track-player';

export function ProgressBar() {
  const { position, duration } = useTrackPlayerProgress();
  const progress = duration
    ? position / duration
    : 0;
  ;
  return (
    <View style={styles.progress}>
      <View style={{ flex: progress, backgroundColor: 'red' }} />
      <View style={{ flex: 1 - progress, backgroundColor: 'grey' }} />
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
