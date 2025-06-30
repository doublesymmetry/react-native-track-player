import { Constants } from '../NativeTrackPlayer';

export enum RepeatMode {
  /** Playback stops when the last track in the queue has finished playing. */
  Off = Constants?.REPEAT_OFF ?? 1,
  /** Repeats the current track infinitely during ongoing playback. */
  Track = Constants?.REPEAT_TRACK ?? 2,
  /** Repeats the entire queue infinitely. */
  Queue = Constants?.REPEAT_QUEUE ?? 3,
}
