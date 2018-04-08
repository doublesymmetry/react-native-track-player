import { AppRegistry } from 'react-native';
import TrackPlayer from 'react-native-track-player';

import App from './App';
import PlayerStore from './react/stores/Player';
import TrackStore, { playbackStates } from './react/stores/Track';

AppRegistry.registerComponent('example', () => App);

TrackPlayer.registerEventHandler(async (data) => {
  if (data.type === 'playback-track-changed') {
    if (data.nextTrack) {
      const track = await TrackPlayer.getTrack(data.nextTrack);
      TrackStore.title = track.title;
      TrackStore.artist = track.artist;
      TrackStore.artwork = track.artwork;
    }
  } else if (data.type === 'playback-state') {
    if (data.state === TrackPlayer.STATE_BUFFERING || data.state === TrackPlayer.STATE_PLAYING) {
      TrackStore.playbackState = playbackStates.playing;
    } else {
      TrackStore.playbackState = playbackStates.halted;
    }
  }
});
