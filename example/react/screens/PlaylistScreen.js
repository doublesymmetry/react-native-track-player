import React, { Component } from 'react';
import { observer } from 'mobx-react';
import TrackPlayer, { ProgressComponent } from 'react-native-track-player';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';

import Player from '../components/Player';
import playlistData from '../data/playlist.json';

import PlayerStore from '../stores/Player';

@observer
export default class LandingScreen extends Component {
  static navigationOptions = {
    title: 'Playlist Example',
  };

  componentDidMount() {
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
      compactCapabilities: [
        TrackPlayer.CAPABILITY_PLAY,
        TrackPlayer.CAPABILITY_PAUSE
      ]
    });
  }

  togglePlayback = async () => {
    const currentTrack = await TrackPlayer.getCurrentTrack();
    if (currentTrack == null) {
      await TrackPlayer.reset();
      await TrackPlayer.add(playlistData);
      await TrackPlayer.play();
    } else {
      if (PlayerStore.playbackState === TrackPlayer.STATE_PAUSED) {
        await TrackPlayer.play();
      } else {
        await TrackPlayer.pause();
      }
    }
  }

  skipToNext = async () => {
    try {
      await TrackPlayer.skipToNext()
    } catch (_) {}
  }

  skipToPrevious = async () => {
    try {
      await TrackPlayer.skipToPrevious()
    } catch (_) {}
  }

  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.description}>
          We'll be inserting a playlist into the library loaded from `playlist.json`.
          We'll also be using the `ProgressComponent` which allows us to track playback time.
        </Text>
        <Player
          style={styles.player}
          onNext={() => this.skipToNext()}
          onPrevious={() => this.skipToPrevious()}
          onTogglePlayback={() => this.togglePlayback()}
        />
        <Text style={styles.state}>{PlayerStore.playbackState}</Text>
      </View>
    );
  }
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
