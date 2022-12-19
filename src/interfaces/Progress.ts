export interface Progress {
  /**
   * The playback position of the current track in seconds.
   * See https://react-native-track-player.js.org/docs/api/functions/player#getposition
   **/
  position: number;
  /** The duration of the current track in seconds.
   * See https://react-native-track-player.js.org/docs/api/functions/player#getduration
   **/
  duration: number;
  /**
   * The buffered position of the current track in seconds.
   **/
  buffered: number;
}
