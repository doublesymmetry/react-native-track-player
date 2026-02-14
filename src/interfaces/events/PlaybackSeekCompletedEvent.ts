export interface PlaybackSeekCompletedEvent {
  /** The position (in seconds) the seek landed at */
  position: number;
  /** Whether the seek operation finished successfully */
  didFinish: boolean;
}
