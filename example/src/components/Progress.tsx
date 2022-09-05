import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import Slider from '@react-native-community/slider';
import TrackPlayer, { useProgress } from 'react-native-track-player';

export const Progress: React.FC = () => {
  const progress = useProgress();
  return (
    <>
      <Slider
        style={styles.container}
        value={progress.position}
        minimumValue={0}
        maximumValue={progress.duration}
        thumbTintColor="#FFD479"
        minimumTrackTintColor="#FFD479"
        maximumTrackTintColor="#FFFFFF"
        onSlidingComplete={value => {
          TrackPlayer.seekTo(value);
        }}
      />
      <View style={styles.labelContainer}>
        <Text style={styles.labelText}>
          {new Date(progress.position * 1000).toISOString().slice(14, 19)}
        </Text>
        <Text style={styles.labelText}>
          {new Date((progress.duration - progress.position) * 1000)
            .toISOString()
            .slice(14, 19)}
        </Text>
      </View>
    </>
  );
};

const styles = StyleSheet.create({
  container: {
    height: 40,
    width: 380,
    marginTop: 25,
    flexDirection: 'row',
  },
  labelContainer: {
    width: 370,
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  labelText: {
    color: 'white',
    fontVariant: ['tabular-nums'],
  },
});
