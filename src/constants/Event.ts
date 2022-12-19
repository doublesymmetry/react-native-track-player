export enum Event {
  /** Fired when the state of the player changes. */
  PlaybackState = 'playback-state',
  /** Fired when a playback error occurs. */
  PlaybackError = 'playback-error',
  /** Fired after playback has paused due to the queue having reached the end. */
  PlaybackQueueEnded = 'playback-queue-ended',
  /**
   * Fired when another track has become active or when there no longer is an
   * active track.
   *
   * @deprecated use `playback-active-track-changed` instead.
   **/
  PlaybackTrackChanged = 'playback-track-changed',
  /**
   * Fired when another track has become active or when there no longer is an
   * active track.
   **/
  PlaybackActiveTrackChanged = 'playback-active-track-changed',
  /**
   * Fired when the current track receives metadata encoded in. (e.g. ID3 tags,
   * Icy Metadata, Vorbis Comments or QuickTime metadata).
   **/
  PlaybackMetadataReceived = 'playback-metadata-received',
  /**
   * Fired when playback play when ready has changed.
   **/
  PlaybackPlayWhenReadyChanged = 'playback-play-when-ready-changed',
  /**
   * Fired when playback progress has been updated.
   * See https://react-native-track-player.js.org/docs/api/events#playbackprogressupdated
   **/
  PlaybackProgressUpdated = 'playback-progress-updated',
  /**
   * Fired when the user presses the play button.
   * See https://react-native-track-player.js.org/docs/api/events#remoteplay
   **/
  RemotePlay = 'remote-play',
  /**
   * Fired when the user presses the pause button.
   * See https://react-native-track-player.js.org/docs/api/events#remotepause
   **/
  RemotePause = 'remote-pause',
  /**
   * Fired when the user presses the stop button.
   * See https://react-native-track-player.js.org/docs/api/events#remotestop
   **/
  RemoteStop = 'remote-stop',
  /**
   * Fired when the user presses the next track button.
   * See https://react-native-track-player.js.org/docs/api/events#remotenext
   **/
  RemoteNext = 'remote-next',
  /**
   * Fired when the user presses the previous track button.
   * See https://react-native-track-player.js.org/docs/api/events#remoteprevious
   **/
  RemotePrevious = 'remote-previous',
  /**
   * Fired when the user presses the jump forward button.
   * See https://react-native-track-player.js.org/docs/api/events#remotejumpforward
   **/
  RemoteJumpForward = 'remote-jump-forward',
  /**
   * Fired when the user presses the jump backward button.
   * See https://react-native-track-player.js.org/docs/api/events#remotejumpbackward
   **/
  RemoteJumpBackward = 'remote-jump-backward',
  /**
   * Fired when the user changes the position of the timeline.
   * See https://react-native-track-player.js.org/docs/api/events#remoteseek
   **/
  RemoteSeek = 'remote-seek',
  /**
   * Fired when the user changes the rating for the track remotely.
   * See https://react-native-track-player.js.org/docs/api/events#remotesetrating
   **/
  RemoteSetRating = 'remote-set-rating',
  /**
   * Fired when the app needs to handle an audio interruption.
   * See https://react-native-track-player.js.org/docs/api/events#remoteduck
   **/
  RemoteDuck = 'remote-duck',
  /**
   * (iOS only) Fired when the user presses the like button in the now playing
   * center.
   * See https://react-native-track-player.js.org/docs/api/events#remotelike-ios-only
   **/
  RemoteLike = 'remote-like',
  /**
   * (iOS only) Fired when the user presses the dislike button in the now playing
   * center.
   * See https://react-native-track-player.js.org/docs/api/events#remotedislike-ios-only
   **/
  RemoteDislike = 'remote-dislike',
  /** (iOS only) Fired when the user presses the bookmark button in the now
   * playing center.
   * See https://react-native-track-player.js.org/docs/api/events#remotebookmark-ios-only
   **/
  RemoteBookmark = 'remote-bookmark',
  /**
   * (Android only) Fired when the user selects a track from an external device.
   * See https://react-native-track-player.js.org/docs/api/events#remoteplayid
   **/
  RemotePlayId = 'remote-play-id',
  /**
   * (Android only) Fired when the user searches for a track (usually voice search).
   * See https://react-native-track-player.js.org/docs/api/events#remoteplaysearch
   **/
  RemotePlaySearch = 'remote-play-search',
  /**
   * (Android only) Fired when the user presses the skip button.
   * See https://react-native-track-player.js.org/docs/api/events#remoteskip
   **/
  RemoteSkip = 'remote-skip',
}
