export interface PlaybackEndedWithReasonEvent {
  /**
   * The reason playback ended.
   * Possible values: 'playedUntilEnd', 'playerStopped', 'skippedToNext',
   * 'skippedToPrevious', 'jumpedToIndex', 'cleared', 'failed'
   * (iOS adds 'cleared' and 'failed'; Android uses UPPER_SNAKE_CASE equivalents)
   */
  reason: string;
  /** The index of the track that was playing */
  track: number;
  /** The playback position (in seconds) when playback ended */
  position: number;
}
