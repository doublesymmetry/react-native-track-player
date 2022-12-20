/************************************************
 * NOTE: this is the entrypoint for web
 ************************************************/
import {AppRegistry} from 'react-native';
import App from './App';
import TrackPlayer from 'react-native-track-player';
import {PlaybackService} from './services';
import 'mux.js';

const appName = 'Your app name';

AppRegistry.registerComponent(appName, () => App);
AppRegistry.runApplication(appName, {
  // Mount the react-native app in the 'root' div of index.html
  rootTag: document.getElementById('root'),
});

TrackPlayer.registerPlaybackService(() => PlaybackService);
