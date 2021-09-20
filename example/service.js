import TrackPlayer, {Event, State} from 'react-native-track-player';

let wasPausedByDuck = false;

module.exports = async function setup() {
  // TrackPlayer.addEventListener(Event.RemotePause, () => {
  //   TrackPlayer.pause();
  // });

  // TrackPlayer.addEventListener(Event.RemotePlay, () => {
  //   TrackPlayer.play();
  // });

  TrackPlayer.addEventListener(Event.RemoteDuck, () => {
    TrackPlayer.pause()
  });
}
