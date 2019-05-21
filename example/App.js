import React, { Component, useEffect } from 'react';
import TrackPlayer, { ProgressComponent } from 'react-native-track-player';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';

import Player from './react/components/Player';
import playlistData from './react/data/playlist.json';
import localTrack from './react/resources/pure.m4a';

import { usePlaybackState, useQueue } from './react/hooks';

const labelByPlaybackState = {
  [TrackPlayer.STATE_NONE]: 'None',
  [TrackPlayer.STATE_PLAYING]: 'Playing',
  [TrackPlayer.STATE_PAUSED]: 'Paused',
  [TrackPlayer.STATE_STOPPED]: 'Stopped',
  [TrackPlayer.STATE_BUFFERING]: 'Buffering'
};

const tracks = [
  ...playlistData,
  {
    id: 'local-track',
    url: localTrack,
    title: 'Pure (Demo)',
    artist: 'David Chavez',
    artwork: 'https://picsum.photos/200'
  }
];

export default function App() {
  const playbackState = usePlaybackState();

  useQueue(tracks);

  useEffect(() => {
    TrackPlayer.setupPlayer();
    TrackPlayer.updateOptions({
      stopWithApp: true,
      capabilities: [
        TrackPlayer.CAPABILITY_PLAY,
        TrackPlayer.CAPABILITY_PAUSE,
        TrackPlayer.CAPABILITY_SKIP_TO_NEXT,
        TrackPlayer.CAPABILITY_SKIP_TO_PREVIOUS,
        TrackPlayer.CAPABILITY_STOP
      ],
      compactCapabilities: [TrackPlayer.CAPABILITY_PLAY, TrackPlayer.CAPABILITY_PAUSE]
    });
  }, []);

  return (
    <View style={styles.container}>
      <Player style={styles.player} />
      <Text style={styles.state}>{labelByPlaybackState[playbackState]}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  description: {
    width: '80%',
    marginTop: 20,
    textAlign: 'center',
  },
  player: {
    marginTop: 40,
  },
  state: {
    marginTop: 20,
  },
});
