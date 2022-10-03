import {
  AppRegistry,
  DeviceEventEmitter,
  NativeEventEmitter,
  NativeModules,
  Platform,
} from 'react-native';
// @ts-expect-error because resolveAssetSource is untyped
import { default as resolveAssetSource } from 'react-native/Libraries/Image/resolveAssetSource';
import {
  Event,
  EventPayloadByEvent,
  MetadataOptions,
  NowPlayingMetadata,
  PlaybackState,
  PlayerOptions,
  Progress,
  RepeatMode,
  State,
  Track,
  TrackMetadataBase,
} from './interfaces';

const { TrackPlayerModule: TrackPlayer } = NativeModules;
const emitter =
  Platform.OS !== 'android'
    ? new NativeEventEmitter(TrackPlayer)
    : DeviceEventEmitter;

// MARK: - Helpers

function resolveImportedPath(path?: number | string) {
  if (!path) return undefined;
  return resolveAssetSource(path) || path;
}

// MARK: - General API

/**
 * Initializes the player with the specified options.
 *
 * @param options The options to initialize the player with.
 * @see https://react-native-track-player.js.org/docs/api/functions/lifecycle
 */
async function setupPlayer(options: PlayerOptions = {}): Promise<void> {
  return TrackPlayer.setupPlayer(options || {});
}

type ServiceHandler = () => Promise<void>;
/**
 * Register the playback service. The service will run as long as the player runs.
 */
function registerPlaybackService(factory: () => ServiceHandler) {
  if (Platform.OS === 'android') {
    // Registers the headless task
    AppRegistry.registerHeadlessTask('TrackPlayer', factory);
  } else {
    // Initializes and runs the service in the next tick
    setImmediate(factory());
  }
}

function addEventListener<T extends Event>(
  event: T,
  listener: EventPayloadByEvent[T] extends never
    ? () => void
    : (event: EventPayloadByEvent[T]) => void
) {
  return emitter.addListener(event, listener);
}

/**
 * @deprecated This method should not be used, most methods reject when service is not bound.
 */
function isServiceRunning(): Promise<boolean> {
  return TrackPlayer.isServiceRunning();
}

// MARK: - Queue API

/**
 * Adds one or more tracks to the queue.
 *
 * @param tracks The tracks to add to the queue.
 * @param insertBeforeIndex (Optional) The index to insert the tracks before.
 * By default the tracks will be added to the end of the queue.
 */
async function add(
  tracks: Track[],
  insertBeforeIndex?: number
): Promise<number | void>;
/**
 * Adds a track to the queue.
 *
 * @param track The track to add to the queue.
 * @param insertBeforeIndex (Optional) The index to insert the track before.
 * By default the track will be added to the end of the queue.
 */
async function add(
  track: Track,
  insertBeforeIndex?: number
): Promise<number | void>;
async function add(
  tracks: Track | Track[],
  insertBeforeIndex = -1
): Promise<number | void> {
  // Clone the array before modifying it
  if (Array.isArray(tracks)) {
    tracks = [...tracks];
  } else {
    tracks = [tracks];
  }

  if (tracks.length < 1) return;

  for (let i = 0; i < tracks.length; i++) {
    // Clone the object before modifying it
    tracks[i] = { ...tracks[i] };

    // Resolve the URLs
    tracks[i].url = resolveImportedPath(tracks[i].url);
    tracks[i].artwork = resolveImportedPath(tracks[i].artwork);
  }

  return TrackPlayer.add(tracks, insertBeforeIndex);
}

/**
 * Replaces the current track or loads the track as the first in the queue.
 *
 * @param track The track to load.
 */
async function load(track: Track): Promise<number | void> {
  return TrackPlayer.load(track);
}

/**
 * Move a track within the queue.
 *
 * @param fromIndex The index of the track to be moved.
 * @param toIndex The index to move the track to. If the index is larger than
 * the size of the queue, then the track is moved to the end of the queue.
 */
async function move(fromIndex: number, toIndex: number): Promise<void> {
  return TrackPlayer.move(fromIndex, toIndex);
}

/**
 * Removes multiple tracks from the queue by their indexes.
 *
 * If the current track is removed, the next track will activated. If the
 * current track was the last track in the queue, the first track will be
 * activated.
 *
 * @param indexes The indexes of the tracks to be removed.
 */
async function remove(indexes: number[]): Promise<void>;
/**
 * Removes a track from the queue by its index.
 *
 * If the current track is removed, the next track will activated. If the
 * current track was the last track in the queue, the first track will be
 * activated.
 *
 * @param index The index of the track to be removed.
 */
async function remove(index: number): Promise<void>;
async function remove(indexOrIndexes: number | number[]): Promise<void> {
  return TrackPlayer.remove(
    Array.isArray(indexOrIndexes) ? indexOrIndexes : [indexOrIndexes]
  );
}

/**
 * Clears any upcoming tracks from the queue.
 */
async function removeUpcomingTracks(): Promise<void> {
  return TrackPlayer.removeUpcomingTracks();
}

/**
 * Skips to a track in the queue.
 *
 * @param index The index of the track to skip to.
 * @param initialPosition (Optional) The initial position to seek to in seconds.
 */
async function skip(index: number, initialPosition = -1): Promise<void> {
  return TrackPlayer.skip(index, initialPosition);
}

/**
 * Skips to the next track in the queue.
 *
 * @param initialPosition (Optional) The initial position to seek to in seconds.
 */
async function skipToNext(initialPosition = -1): Promise<void> {
  return TrackPlayer.skipToNext(initialPosition);
}

/**
 * Skips to the previous track in the queue.
 *
 * @param initialPosition (Optional) The initial position to seek to in seconds.
 */
async function skipToPrevious(initialPosition = -1): Promise<void> {
  return TrackPlayer.skipToPrevious(initialPosition);
}

// MARK: - Control Center / Notifications API

/**
 * Updates the configuration for the components.
 *
 * @param options The options to update.
 * @see https://react-native-track-player.js.org/docs/api/functions/player#updateoptionsoptions
 */
async function updateOptions(options: MetadataOptions = {}): Promise<void> {
  options = { ...options };

  // Resolve the asset for each icon
  options.icon = resolveImportedPath(options.icon);
  options.playIcon = resolveImportedPath(options.playIcon);
  options.pauseIcon = resolveImportedPath(options.pauseIcon);
  options.stopIcon = resolveImportedPath(options.stopIcon);
  options.previousIcon = resolveImportedPath(options.previousIcon);
  options.nextIcon = resolveImportedPath(options.nextIcon);
  options.rewindIcon = resolveImportedPath(options.rewindIcon);
  options.forwardIcon = resolveImportedPath(options.forwardIcon);

  return TrackPlayer.updateOptions(options);
}

/**
 * Updates the metadata of a track in the queue. If the current track is updated,
 * the notification and the Now Playing Center will be updated accordingly.
 *
 * @param trackIndex The index of the track whose metadata will be updated.
 * @param metadata The metadata to update.
 */
async function updateMetadataForTrack(
  trackIndex: number,
  metadata: TrackMetadataBase
): Promise<void> {
  // Clone the object before modifying it
  metadata = Object.assign({}, metadata);

  // Resolve the artwork URL
  metadata.artwork = resolveImportedPath(metadata.artwork);

  return TrackPlayer.updateMetadataForTrack(trackIndex, metadata);
}

function clearNowPlayingMetadata(): Promise<void> {
  return TrackPlayer.clearNowPlayingMetadata();
}

function updateNowPlayingMetadata(metadata: NowPlayingMetadata): Promise<void> {
  // Clone the object before modifying it
  metadata = Object.assign({}, metadata);

  // Resolve the artwork URL
  metadata.artwork = resolveImportedPath(metadata.artwork);

  return TrackPlayer.updateNowPlayingMetadata(metadata);
}

// MARK: - Player API

/**
 * Resets the player stopping the current track and clearing the queue.
 */
async function reset(): Promise<void> {
  return TrackPlayer.reset();
}

/**
 * Plays or resumes the current track.
 */
async function play(): Promise<void> {
  return TrackPlayer.play();
}

/**
 * Pauses the current track.
 */
async function pause(): Promise<void> {
  return TrackPlayer.pause();
}

/**
 * Sets wether the player will play automatically when it is ready to do so.
 * This is the equivalent of calling `TrackPlayer.play()` when `playWhenReady = true`
 * or `TrackPlayer.pause()` when `playWhenReady = false`.
 */
async function setPlayWhenReady(playWhenReady: boolean): Promise<boolean> {
  return TrackPlayer.setPlayWhenReady(playWhenReady);
}

/**
 * Gets wether the player will play automatically when it is ready to do so.
 */
async function getPlayWhenReady(): Promise<boolean> {
  return TrackPlayer.getPlayWhenReady();
}

/**
 * Seeks to a specified time position in the current track.
 *
 * @param position The position to seek to in seconds.
 */
async function seekTo(position: number): Promise<void> {
  return TrackPlayer.seekTo(position);
}

/**
 * Sets the volume of the player.
 *
 * @param volume The volume as a number between 0 and 1.
 */
async function setVolume(level: number): Promise<void> {
  return TrackPlayer.setVolume(level);
}

/**
 * Sets the playback rate.
 *
 * @param rate The playback rate to change to, where 0.5 would be half speed,
 * 1 would be regular speed, 2 would be double speed etc.
 */
async function setRate(rate: number): Promise<void> {
  return TrackPlayer.setRate(rate);
}

/**
 * Sets the queue repeat mode.
 *
 * @param repeatMode The repeat mode to set.
 * @see https://react-native-track-player.js.org/docs/api/constants/repeat-mode
 */
async function setRepeatMode(mode: RepeatMode): Promise<RepeatMode> {
  return TrackPlayer.setRepeatMode(mode);
}

// MARK: - Getters

/**
 * Gets the volume of the player as a number between 0 and 1.
 */
async function getVolume(): Promise<number> {
  return TrackPlayer.getVolume();
}

/**
 * Gets the playback rate where 0.5 would be half speed, 1 would be
 * regular speed and 2 would be double speed etc.
 */
async function getRate(): Promise<number> {
  return TrackPlayer.getRate();
}

/**
 * Gets a track object from the queue.
 *
 * @param index The index of the track.
 * @returns The track object or null if there isn't a track object at that index.
 */
async function getTrack(index: number): Promise<Track | null> {
  return TrackPlayer.getTrack(index);
}

/**
 * Gets the whole queue.
 */
async function getQueue(): Promise<Track[]> {
  return TrackPlayer.getQueue();
}

/**
 * Gets the index of the current track or null if there is no current track.
 */
async function getCurrentTrack(): Promise<number | null> {
  return TrackPlayer.getCurrentTrack();
}

/**
 * Gets the duration of the current track in seconds.
 * @deprecated Use `TrackPlayer.getProgress().then((progress) => progress.buffered)` instead.
 */
async function getDuration(): Promise<number> {
  return TrackPlayer.getDuration();
}

/**
 * Gets the buffered position of the current track in seconds.
 *
 * @deprecated Use `TrackPlayer.getProgress().then((progress) => progress.buffered)` instead.
 */
async function getBufferedPosition(): Promise<number> {
  return TrackPlayer.getBufferedPosition();
}

/**
 * Gets the playback position of the current track in seconds.
 * @deprecated Use `TrackPlayer.getProgress().then((progress) => progress.position)` instead.
 */
async function getPosition(): Promise<number> {
  return TrackPlayer.getPosition();
}

/**
 * Gets information on the progress of the currently active track, including its
 * current playback position in seconds, buffered position in seconds and
 * duration in seconds.
 */
async function getProgress(): Promise<Progress> {
  return TrackPlayer.getProgress();
}

/**
 * @deprecated use (await getPlaybackState()).state instead.
 */
async function getState(): Promise<State> {
  return TrackPlayer.getState();
}

/**
 * Gets the playback state of the player.
 *
 * @see https://react-native-track-player.js.org/docs/api/constants/state
 */
async function getPlaybackState(): Promise<PlaybackState> {
  return TrackPlayer.getPlaybackState();
}

/**
 * Gets the queue repeat mode.
 *
 * @see https://react-native-track-player.js.org/docs/api/constants/repeat-mode
 */
async function getRepeatMode(): Promise<RepeatMode> {
  return TrackPlayer.getRepeatMode();
}

/**
 * Reloads the current item.
 */
async function reload({
  startFromCurrentTime = true,
}: { startFromCurrentTime?: boolean } = {}) {
  return TrackPlayer.reload({ startFromCurrentTime });
}

export default {
  // MARK: - General API
  setupPlayer,
  registerPlaybackService,
  addEventListener,
  isServiceRunning,

  // MARK: - Queue API
  add,
  load,
  reload,
  reset,
  move,
  remove,
  removeUpcomingTracks,
  skip,
  skipToNext,
  skipToPrevious,

  // MARK: - Control Center / Notifications API
  updateOptions,
  updateMetadataForTrack,
  clearNowPlayingMetadata,
  updateNowPlayingMetadata,

  // MARK: - Player API
  play,
  pause,
  getPlayWhenReady,
  setPlayWhenReady,
  seekTo,
  setVolume,
  setRate,
  setRepeatMode,

  // MARK: - Getters
  getVolume,
  getRate,
  getTrack,
  getQueue,
  getCurrentTrack,
  getDuration,
  getBufferedPosition,
  getPosition,
  getProgress,
  getState,
  getPlaybackState,
  getRepeatMode,
};
