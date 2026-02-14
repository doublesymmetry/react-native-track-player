export interface PlaybackErrorLogEntry {
  /** The HTTP status code of the error (e.g. 404, 503). */
  errorStatusCode: number;
  /** The domain of the error. */
  errorDomain: string;
  /** A human-readable comment describing the error, if available. */
  errorComment?: string;
  /** The URI that caused the error, if available. */
  uri?: string;
  /** The server address, if available. */
  serverAddress?: string;
  /** The timestamp of the error as seconds since epoch. */
  date: number;
}

export interface PlaybackErrorLogEvent {
  /** Array of error log entries. Typically contains the most recent entry. */
  entries: PlaybackErrorLogEntry[];
}
