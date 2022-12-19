export interface PlaybackQueueEndedEvent {
  /** The index of the active track when the playback queue ended. */
  track: number;
  /**
   * The playback position in seconds of the active track when the playback
   * queue ended.
   **/
  position: number;
}
