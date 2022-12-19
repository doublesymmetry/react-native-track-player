import { NativeModules } from 'react-native';
const { TrackPlayerModule: TrackPlayer } = NativeModules;

export enum RepeatMode {
  /** Playback stops when the last track in the queue has finished playing. */
  Off = TrackPlayer.REPEAT_OFF,
  /** Repeats the current track infinitely during ongoing playback. */
  Track = TrackPlayer.REPEAT_TRACK,
  /** Repeats the entire queue infinitely. */
  Queue = TrackPlayer.REPEAT_QUEUE,
}
