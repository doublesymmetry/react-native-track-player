import { NativeModules } from 'react-native';
import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource';

const { TrackPlayerModule } = NativeModules;

const resolveAsset = uri => (!uri ? undefined : resolveAssetSource(uri));
const resolveUrl = url => (!url ? undefined : resolveAssetSource(url) || url);

const setupPlayer = options => TrackPlayerModule.setupPlayer(options || {});

const updateOptions = data => {
  // Clone the object before modifying it, so we don't run into problems with immutable objects
  data = Object.assign({}, data);

  // Resolve the asset for each icon
  data.icon = resolveAsset(data.icon);
  data.playIcon = resolveAsset(data.playIcon);
  data.pauseIcon = resolveAsset(data.pauseIcon);
  data.stopIcon = resolveAsset(data.stopIcon);
  data.previousIcon = resolveAsset(data.previousIcon);
  data.nextIcon = resolveAsset(data.nextIcon);
  data.rewindIcon = resolveAsset(data.rewindIcon);
  data.forwardIcon = resolveAsset(data.forwardIcon);

  return TrackPlayerModuleckPlayer.updateOptions(data);
};

const add = (tracks, insertBeforeId) => {
  if (!Array.isArray(tracks)) {
    tracks = [tracks];
  }

  if (tracks.length < 1) return;

  for (let i = 0; i < tracks.length; i++) {
    // Clone the object before modifying it
    tracks[i] = Object.assign({}, tracks[i]);

    // Resolve the URLs
    tracks[i].url = resolveUrl(tracks[i].url);
    tracks[i].artwork = resolveUrl(tracks[i].artwork);

    // Cast ID's into strings
    tracks[i].id = `${tracks[i].id}`;
  }

  return TrackPlayerModule.add(tracks, insertBeforeId);
};

const remove = tracks => {
  if (!Array.isArray(tracks)) {
    tracks = [tracks];
  }

  return TrackPlayerModule.remove(tracks);
};

export const TrackPlayer = {
  ...TrackPlayerModule,
  // We'll declare each one of the constants and functions manually so IDEs can show a list of them
  // We should also add documentation here, but I'll leave this task to another day
  // States
  STATE_NONE: TrackPlayerModule.STATE_NONE,
  STATE_READY: TrackPlayerModule.STATE_READY,
  STATE_PLAYING: TrackPlayerModule.STATE_PLAYING,
  STATE_PAUSED: TrackPlayerModule.STATE_PAUSED,
  STATE_STOPPED: TrackPlayerModule.STATE_STOPPED,
  STATE_BUFFERING: TrackPlayerModule.STATE_BUFFERING,
  STATE_CONNECTING: TrackPlayerModule.STATE_CONNECTING,
  // Capabilities
  CAPABILITY_PLAY: TrackPlayerModule.CAPABILITY_PLAY,
  CAPABILITY_PLAY_FROM_ID: TrackPlayerModule.CAPABILITY_PLAY_FROM_ID,
  CAPABILITY_PLAY_FROM_SEARCH: TrackPlayerModule.CAPABILITY_PLAY_FROM_SEARCH,
  CAPABILITY_PAUSE: TrackPlayerModule.CAPABILITY_PAUSE,
  CAPABILITY_STOP: TrackPlayerModule.CAPABILITY_STOP,
  CAPABILITY_SEEK_TO: TrackPlayerModule.CAPABILITY_SEEK_TO,
  CAPABILITY_SKIP: TrackPlayerModule.CAPABILITY_SKIP,
  CAPABILITY_SKIP_TO_NEXT: TrackPlayerModule.CAPABILITY_SKIP_TO_NEXT,
  CAPABILITY_SKIP_TO_PREVIOUS: TrackPlayerModule.CAPABILITY_SKIP_TO_PREVIOUS,
  CAPABILITY_JUMP_FORWARD: TrackPlayerModule.CAPABILITY_JUMP_FORWARD,
  CAPABILITY_JUMP_BACKWARD: TrackPlayerModule.CAPABILITY_JUMP_BACKWARD,
  CAPABILITY_SET_RATING: TrackPlayerModule.CAPABILITY_SET_RATING,
  CAPABILITY_LIKE: TrackPlayerModule.CAPABILITY_LIKE,
  CAPABILITY_DISLIKE: TrackPlayerModule.CAPABILITY_DISLIKE,
  CAPABILITY_BOOKMARK: TrackPlayerModule.CAPABILITY_BOOKMARK,
  // Pitch algorithms
  PITCH_ALGORITHM_LINEAR: TrackPlayerModule.PITCH_ALGORITHM_LINEAR,
  PITCH_ALGORITHM_MUSIC: TrackPlayerModule.PITCH_ALGORITHM_MUSIC,
  PITCH_ALGORITHM_VOICE: TrackPlayerModule.PITCH_ALGORITHM_VOICE,
  // Rating Types
  RATING_HEART: TrackPlayerModule.RATING_HEART,
  RATING_THUMBS_UP_DOWN: TrackPlayerModule.RATING_THUMBS_UP_DOWN,
  RATING_3_STARS: TrackPlayerModule.RATING_3_STARS,
  RATING_4_STARS: TrackPlayerModule.RATING_4_STARS,
  RATING_5_STARS: TrackPlayerModule.RATING_5_STARS,
  RATING_PERCENTAGE: TrackPlayerModule.RATING_PERCENTAGE,
  // General
  setupPlayer: setupPlayer,
  destroy: TrackPlayerModule.destroy,
  updateOptions: updateOptions,
  // Player Queue Commands
  add,
  remove,
  skip: TrackPlayerModule.skip,
  getQueue: TrackPlayerModule.getQueue,
  skipToNext: TrackPlayerModule.skipToNext,
  skipToPrevious: TrackPlayerModule.skipToPrevious,
  updateMetadataForTrack: TrackPlayerModule.updateMetadataForTrack,
  removeUpcomingTracks: TrackPlayerModule.removeUpcomingTracks,
  // Player Playback Commands
  reset: TrackPlayerModule.reset,
  play: TrackPlayerModule.play,
  pause: TrackPlayerModule.pause,
  stop: TrackPlayerModule.stop,
  seekTo: TrackPlayerModule.seekTo,
  setVolume: TrackPlayerModule.setVolume,
  setRate: TrackPlayerModule.setRate,
  // Player Getters
  getTrack: TrackPlayerModule.getTrack,
  getCurrentTrack: TrackPlayerModule.getCurrentTrack,
  getVolume: TrackPlayerModule.getVolume,
  getDuration: TrackPlayerModule.getDuration,
  getPosition: TrackPlayerModule.getPosition,
  getBufferedPosition: TrackPlayerModule.getBufferedPosition,
  getState: TrackPlayerModule.getState,
  getRate: TrackPlayerModule.getRate,
};
export default TrackPlayer;
