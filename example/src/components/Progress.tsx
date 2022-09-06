import Slider from '@react-native-community/slider';
import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import TrackPlayer, { useProgress } from 'react-native-track-player';

export const Progress: React.FC<{ live?: boolean }> = ({ live }) => {
  const progress = useProgress();
  return live ? (
    <View style={styles.liveContainer}>
      <Text style={styles.liveText}>Live Stream</Text>
    </View>
  ) : (
    <>
      <Slider
        style={styles.container}
        value={progress.position}
        minimumValue={0}
        maximumValue={progress.duration}
        thumbTintColor="#FFD479"
        minimumTrackTintColor="#FFD479"
        maximumTrackTintColor="#FFFFFF"
        onSlidingComplete={TrackPlayer.seekTo}
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
  liveContainer: {
    height: 100,
    alignItems: 'center',
    flexDirection: 'row',
  },
  liveText: {
    color: 'white',
    alignSelf: 'center',
    fontSize: 18,
  },
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
