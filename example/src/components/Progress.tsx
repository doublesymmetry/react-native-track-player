import Slider from '@react-native-community/slider';
import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import TrackPlayer, { useProgress } from 'react-native-track-player';

export const Progress: React.FC<{ live?: boolean }> = ({ live }) => {
  const { position, duration } = useProgress();
  return live ? (
    <View style={styles.liveContainer}>
      <Text style={styles.liveText}>Live Stream</Text>
    </View>
  ) : (
    <View>
      <Slider
        style={styles.container}
        value={position}
        minimumValue={0}
        maximumValue={duration}
        thumbTintColor="#FFD479"
        minimumTrackTintColor="#FFD479"
        maximumTrackTintColor="#FFFFFF"
        onSlidingComplete={TrackPlayer.seekTo}
      />
      <View style={styles.labelContainer}>
        <Text style={styles.labelText}>{formatSeconds(position)}</Text>
        <Text style={styles.labelText}>
          {formatSeconds(Math.max(0, duration - position))}
        </Text>
      </View>
    </View>
  );
};

const formatSeconds = (time: number) =>
  new Date(time * 1000).toISOString().slice(14, 19);

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
