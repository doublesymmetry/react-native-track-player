/**
 * @description
 *   Available event types that can be emitted from TrackPlayer.
 */
const TrackPlayerEvents = {
  REMOTE_PLAY: 'remote-play',
  REMOTE_PLAY_ID: 'remote-play-id',
  REMOTE_PLAY_SEARCH: 'remote-play-search',
  REMOTE_PAUSE: 'remote-pause',
  REMOTE_STOP: 'remote-stop',
  REMOTE_SKIP: 'remote-skip',
  REMOTE_NEXT: 'remote-next',
  REMOTE_PREVIOUS: 'remote-previous',
  REMOTE_SEEK: 'remote-seek',
  REMOTE_SET_RATING: 'remote-set-rating',
  REMOTE_JUMP_FORWARD: 'remote-jump-forward',
  REMOTE_JUMP_BACKWARD: 'remote-jump-backward',
  REMOTE_DUCK: 'remote-duck',
  REMOTE_LIKE: 'remote-like',
  REMOTE_DISLIKE: 'remote-dislike',
  REMOTE_BOOKMARK: 'remote-bookmark',
  PLAYBACK_STATE: 'playback-state',
  PLAYBACK_TRACK_CHANGED: 'playback-track-changed',
  PLAYBACK_QUEUE_ENDED: 'playback-queue-ended',
  PLAYBACK_ERROR: 'playback-error',
  PLAYBACK_METADATA_RECEIVED: 'playback-metadata-received'
}

module.exports = TrackPlayerEvents
