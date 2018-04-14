import React, { Component } from 'react';
import { StackNavigator } from 'react-navigation';

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
  render() {
    return (
      <RootStack />
    );
  }
}
