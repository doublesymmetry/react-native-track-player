export interface PlaybackStalledEvent {
  /** The index of the track that was playing when the stall occurred. */
  track: number;
  /** The playback position (in seconds) when the stall occurred. */
  position: number;
}
