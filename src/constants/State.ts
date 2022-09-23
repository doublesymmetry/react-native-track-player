import { NativeModules } from 'react-native';
const { TrackPlayerModule: TrackPlayer } = NativeModules;

export enum State {
  /** Indicates that no media is currently loaded */
  None = TrackPlayer.STATE_NONE,
  /** Indicates that the player is ready to start playing */
  Ready = TrackPlayer.STATE_READY,
  /** Indicates that the player is currently playing */
  Playing = TrackPlayer.STATE_PLAYING,
  /** Indicates that the player is currently paused */
  Paused = TrackPlayer.STATE_PAUSED,
  /** Indicates that the player is currently stopped */
  Stopped = TrackPlayer.STATE_STOPPED,
  /** Indicates that the player is currently buffering (in "play" state) */
  Buffering = TrackPlayer.STATE_BUFFERING,
  /** Indicates that the player is currently buffering (in "pause" state) */
  Connecting = TrackPlayer.STATE_CONNECTING,
}
