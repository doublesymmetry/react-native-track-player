import React from 'react';
import {
  Image,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import type {Track} from 'react-native-track-player';

export interface TrackInfoProps {
  track?: Track;
}

export const TrackInfo: React.FC<TrackInfoProps> = ({ track }) => {
  return (
    <View style={styles.container}>
      <Image style={styles.artwork} source={{uri: `${track?.artwork}`}} />
      <Text style={styles.titleText}>{track?.title}</Text>
      <Text style={styles.artistText}>{track?.artist}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
  },
  artwork: {
    width: 240,
    height: 240,
    marginTop: 30,
    backgroundColor: 'grey',
  },
  titleText: {
    fontSize: 18,
    fontWeight: '600',
    color: 'white',
    marginTop: 30,
  },
  artistText: {
    fontSize: 16,
    fontWeight: '200',
    color: 'white',
  },
});
