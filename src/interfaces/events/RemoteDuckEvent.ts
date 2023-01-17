export interface RemoteDuckEvent {
  /**
   * On Android when true the player should pause playback, when false the
   * player may resume playback. On iOS when true the playback was paused and
   * when false the player may resume playback.
   **/
  paused: boolean;
  /**
   * Whether the interruption is permanent. On Android the player should stop
   * playback.
   **/
  permanent: boolean;
}
