import type { Track } from '../Track';

export interface PlaybackActiveTrackChangedEvent {
  /** The index of previously active track. */
  lastIndex?: number;

  /**
   * The previously active track or `undefined` when there wasn't a previously
   * active track.
   */
  lastTrack?: Track;

  /**
   * The position of the previously active track in seconds.
   */
  lastPosition: number;

  /**
   * The newly active track index or `undefined` if there is no longer an
   * active track.
   */
  index?: number;

  /**
   * The newly active track or `undefined` if there is no longer an
   * active track.
   */
  track?: Track;
}
