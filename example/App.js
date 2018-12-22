import React, { Component } from 'react';
import TrackPlayer from 'react-native-track-player';
import { StackNavigator } from 'react-navigation';

import PlayerStore from './react/stores/Player';
import TrackStore from './react/stores/Track';

import LandingScreen from './react/screens/LandingScreen';
import PlaylistScreen from './react/screens/PlaylistScreen';

const RootStack = StackNavigator({
  Landing: {
    screen: LandingScreen,
  },
  Playlist: {
    screen: PlaylistScreen,
  },
}, { initialRouteName: 'Landing' })

export default class App extends Component {

  componentDidMount() {
    this._onTrackChanged = TrackPlayer.addEventListener('playback-track-changed', async (data) => {
      if (data.nextTrack) {
        const track = await TrackPlayer.getTrack(data.nextTrack);
        TrackStore.title = track.title;
        TrackStore.artist = track.artist;
        TrackStore.artwork = track.artwork;
      }
    })

    this._onStateChanged = TrackPlayer.addEventListener('playback-state', (data) => {
      PlayerStore.playbackState = data.state;
    })
  }

  componentWillUnmount() {
    this._onTrackChanged.remove()
    this._onStateChanged.remove()
  }

  render() {
    return (
      <RootStack />
    );
  }
}
