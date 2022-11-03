import {
  AppRegistry,
  DeviceEventEmitter,
  NativeEventEmitter,
  NativeModules,
  Platform
} from 'react-native';
// @ts-expect-error because resolveAssetSource is untyped
import { default as resolveAssetSource } from 'react-native/Libraries/Image/resolveAssetSource';
import {
  Event,
  EventPayloadByEvent,
  NowPlayingMetadata,
  PlaybackState,
  PlayerOptions,
  Progress,
  RepeatMode,
  SleepTimer,
  State,
  Track,
  TrackMetadataBase,
  UpdateOptions
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
  return TrackPlayer.setupPlayer(options);
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
async function updateOptions({
  alwaysPauseOnInterruption,
  ...options
}: UpdateOptions = {}): Promise<void> {

  // Handle deprecated alwaysPauseOnInterruption option:
  if (
    alwaysPauseOnInterruption !== undefined &&
    !(options.android && 'alwaysPauseOnInterruption' in options.android)
  ) {
    if (!options.android) options.android = {};
    options.android.alwaysPauseOnInterruption = alwaysPauseOnInterruption;
  }

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
 * Stops the current track.
 */
async function stop(): Promise<void> {
  return TrackPlayer.stop();
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
 * Seeks by a relative time offset in the current track.
 *
 * @param offset The time offset to seek by in seconds.
 */
async function seekBy(offset: number): Promise<void> {
  return TrackPlayer.seekBy(offset);
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
 * Sets the queue.
 *
 * @param tracks The tracks to set as the queue.
 * @see https://react-native-track-player.js.org/docs/api/constants/repeat-mode
 */
async function setQueue(tracks: Track[]): Promise<void> {
  return TrackPlayer.setQueue(tracks);
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
 * @returns The track object or undefined if there isn't a track object at that
 * index.
 */
async function getTrack(index: number): Promise<Track | undefined> {
  return TrackPlayer.getTrack(index);
}

/**
 * Gets the whole queue.
 */
async function getQueue(): Promise<Track[]> {
  return TrackPlayer.getQueue();
}

/**
 * Gets the index of the active track in the queue or undefined if there is no
 * current track.
 */
async function getActiveTrackIndex(): Promise<number | undefined> {
  return (await TrackPlayer.getActiveTrackIndex()) ?? undefined;
}

/**
 * Gets the active track or undefined if there is no current track.
 */
async function getActiveTrack(): Promise<Track | undefined> {
  return (await TrackPlayer.getActiveTrack()) ?? undefined;
}

/**
 * Gets the index of the current track or null if there is no current track.
 *
 * @deprecated use `TrackPlayer.getActiveTrackIndex()` instead.
 */
async function getCurrentTrack(): Promise<number | null> {
  return TrackPlayer.getActiveTrackIndex();
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
  return (await TrackPlayer.getPlaybackState()).state;
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
 * Retries the current item when the playback state is `State.Error`.
 */
async function retry() {
  return TrackPlayer.retry();
}

/**
 * Sets a sleep timer to fire after a specified amount of seconds.
 *
 * Note that if a sleep timer was set previously, it will be replaced by the
 * new one.
 */
 async function setSleepTimer(seconds: number): Promise<SleepTimer> {
    if (seconds <= 0) {
      throw new Error('The sleep timer must be greater than 0.');
    }
    return TrackPlayer.setSleepTimer(seconds);
}

/**
 * Pauses playback when the active track ends. Note that this will override any
 * sleep timer that was set previously. To clear call `TrackPlayer.clearSleepTimer()`.
 */
 async function sleepWhenActiveTrackReachesEnd(): Promise<void> {
  return TrackPlayer.sleepWhenActiveTrackReachesEnd();
}

/**
 * Gets information on the current sleep timer. Returns `null` if there is no
 * sleep timer set.
 */
 async function getSleepTimer(): Promise<SleepTimer> {
  return (await TrackPlayer.getSleepTimer()) ?? undefined;
}

/**
 * Clears the sleep timer if it was set previously.
 *
 * Note that it is not necessary to clear the sleep timer before setting a new
 * one.
 */
 async function clearSleepTimer(): Promise<void> {
  return TrackPlayer.clearSleepTimer();
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
  retry,
  reset,
  move,
  remove,
  removeUpcomingTracks,
  skip,
  skipToNext,
  skipToPrevious,
  setQueue,

  // MARK: - Control Center / Notifications API
  updateOptions,
  updateMetadataForTrack,
  clearNowPlayingMetadata,
  updateNowPlayingMetadata,

  // MARK: - Player API
  play,
  pause,
  stop,
  getPlayWhenReady,
  setPlayWhenReady,
  seekTo,
  seekBy,
  setVolume,
  setRate,
  setRepeatMode,

  // MARK: - Sleep Timer API
  setSleepTimer,
  clearSleepTimer,
  sleepWhenActiveTrackReachesEnd,
  getSleepTimer: getSleepTimer,

  // MARK: - Getters
  getVolume,
  getRate,
  getTrack,
  getQueue,
  getCurrentTrack,
  getActiveTrackIndex,
  getActiveTrack,
  getDuration,
  getBufferedPosition,
  getPosition,
  getProgress,
  getState,
  getPlaybackState,
  getRepeatMode,
};
