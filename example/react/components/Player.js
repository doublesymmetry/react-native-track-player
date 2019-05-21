import PropTypes from 'prop-types';
import React from 'react';
import { Image, StyleSheet, Text, View } from 'react-native';

import { ProgressBar } from './ProgressBar';
import { Controls } from './Controls';
import { useCurrentTrack, useQueue } from '../hooks';

export default function Player({ style }) {
  const currentTrack = useCurrentTrack() || {};
  return (
    <>
      <View style={[styles.card, style]}>
        <Image style={styles.cover} source={{ uri: currentTrack.artwork }} />
        <ProgressBar />
        <Text style={styles.title}>{currentTrack.title}</Text>
        <Text style={styles.artist}>{currentTrack.artist}</Text>
        <Controls />
      </View>
    </>
  );
}

const styles = StyleSheet.create({
  card: {
    width: '80%',
    elevation: 1,
    borderRadius: 4,
    shadowRadius: 2,
    shadowOpacity: 0.1,
    alignItems: 'center',
    shadowColor: 'black',
    backgroundColor: 'white',
    shadowOffset: { width: 0, height: 1 }
  },
  cover: {
    width: 140,
    height: 140,
    marginTop: 20,
    backgroundColor: 'grey'
  },
  title: {
    marginTop: 10
  },
  artist: {
    fontWeight: 'bold'
  }
});
