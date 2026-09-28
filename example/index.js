import { AppRegistry } from 'react-native';
import TrackPlayer from '@rntp/player';
import { startPlaybackSession } from './src/services/playbackSession';
import { name as appName } from './app.json';

TrackPlayer.registerPlaybackSession(startPlaybackSession);
AppRegistry.registerComponent(appName, () => require('./src/App').default);
