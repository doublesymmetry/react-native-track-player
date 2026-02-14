export interface PlaybackErrorEvent {
  /** The error code */
  code: string;
  /** The error message */
  message: string;
  /** The playback position (in seconds) when the error occurred */
  position?: number;
}
