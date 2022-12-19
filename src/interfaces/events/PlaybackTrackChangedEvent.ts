/**
 * @deprecated
 */
export interface PlaybackTrackChangedEvent {
  /** The index of previously active track. */
  track: number | null;
  /** The previous track position in seconds. */
  position: number;
  /** The next (active) track index. */
  nextTrack: number;
}
