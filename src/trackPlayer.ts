import { Platform, AppRegistry, DeviceEventEmitter, NativeEventEmitter, NativeModules } from 'react-native'
// @ts-ignore
import * as resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource'
import {
  MetadataOptions,
  PlayerOptions,
  Event,
  Track,
  State,
  TrackMetadataBase,
  NowPlayingMetadata,
  RepeatMode,
} from './interfaces'

const { TrackPlayerModule: TrackPlayer } = NativeModules
const emitter = Platform.OS !== 'android' ? new NativeEventEmitter(TrackPlayer) : DeviceEventEmitter

// MARK: - Helpers

function resolveImportedPath(path?: number | string) {
  if (!path) return undefined
  return resolveAssetSource(path) || path
}

// MARK: - General API

async function setupPlayer(options: PlayerOptions = {}): Promise<void> {
  return TrackPlayer.setupPlayer(options || {})
}

function destroy(): Promise<void> {
  emitter.removeAllListeners()
  return TrackPlayer.destroy()
}

type ServiceHandler = () => Promise<void>
function registerPlaybackService(factory: () => ServiceHandler) {
  if (Platform.OS === 'android') {
    // Registers the headless task
    AppRegistry.registerHeadlessTask('TrackPlayer', factory)
  } else {
    // Initializes and runs the service in the next tick
    setImmediate(factory())
  }
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function addEventListener(event: Event, listener: (data: any) => void) {
  return emitter.addListener(event, listener)
}

// MARK: - Queue API

async function add(tracks: Track | Track[], insertBeforeIndex?: number): Promise<void> {
  // Clone the array before modifying it
  if (Array.isArray(tracks)) {
    tracks = [...tracks]
  } else {
    tracks = [tracks]
  }

  if (tracks.length < 1) return

  for (let i = 0; i < tracks.length; i++) {
    // Clone the object before modifying it
    tracks[i] = { ...tracks[i] }

    // Resolve the URLs
    tracks[i].url = resolveImportedPath(tracks[i].url)
    tracks[i].artwork = resolveImportedPath(tracks[i].artwork)
  }

  // Note: we must be careful about passing nulls to non nullable parameters on Android.
  return TrackPlayer.add(tracks, insertBeforeIndex === undefined ? -1 : insertBeforeIndex)
}

async function remove(tracks: number | number[]): Promise<void> {
  if (!Array.isArray(tracks)) {
    tracks = [tracks]
  }

  return TrackPlayer.remove(tracks)
}

async function removeUpcomingTracks(): Promise<void> {
  return TrackPlayer.removeUpcomingTracks()
}

async function skip(trackIndex: number): Promise<void> {
  return TrackPlayer.skip(trackIndex)
}

async function skipToNext(): Promise<void> {
  return TrackPlayer.skipToNext()
}

async function skipToPrevious(): Promise<void> {
  return TrackPlayer.skipToPrevious()
}

// MARK: - Control Center / Notifications API

async function updateOptions(options: MetadataOptions = {}): Promise<void> {
  options = { ...options }

  // Resolve the asset for each icon
  options.icon = resolveImportedPath(options.icon)
  options.playIcon = resolveImportedPath(options.playIcon)
  options.pauseIcon = resolveImportedPath(options.pauseIcon)
  options.stopIcon = resolveImportedPath(options.stopIcon)
  options.previousIcon = resolveImportedPath(options.previousIcon)
  options.nextIcon = resolveImportedPath(options.nextIcon)
  options.rewindIcon = resolveImportedPath(options.rewindIcon)
  options.forwardIcon = resolveImportedPath(options.forwardIcon)

  return TrackPlayer.updateOptions(options)
}

async function updateMetadataForTrack(trackIndex: number, metadata: TrackMetadataBase): Promise<void> {
  // Clone the object before modifying it
  metadata = Object.assign({}, metadata)

  // Resolve the artwork URL
  metadata.artwork = resolveImportedPath(metadata.artwork)

  return TrackPlayer.updateMetadataForTrack(trackIndex, metadata)
}

function clearNowPlayingMetadata(): Promise<void> {
  return TrackPlayer.clearNowPlayingMetadata()
}

function updateNowPlayingMetadata(metadata: NowPlayingMetadata): Promise<void> {
  // Clone the object before modifying it
  metadata = Object.assign({}, metadata)

  // Resolve the artwork URL
  metadata.artwork = resolveImportedPath(metadata.artwork)

  return TrackPlayer.updateNowPlayingMetadata(metadata)
}

// MARK: - Player API

async function reset(): Promise<void> {
  return TrackPlayer.reset()
}

async function play(): Promise<void> {
  return TrackPlayer.play()
}

async function pause(): Promise<void> {
  return TrackPlayer.pause()
}

async function stop(): Promise<void> {
  return TrackPlayer.stop()
}

async function seekTo(position: number): Promise<void> {
  return TrackPlayer.seekTo(position)
}

async function setVolume(level: number): Promise<void> {
  return TrackPlayer.setVolume(level)
}

async function setRate(rate: number): Promise<void> {
  return TrackPlayer.setRate(rate)
}

async function setRepeatMode(mode: RepeatMode): Promise<RepeatMode> {
  return TrackPlayer.setRepeatMode(mode)
}

// MARK: - Getters

async function getVolume(): Promise<number> {
  return TrackPlayer.getVolume()
}

async function getRate(): Promise<number> {
  return TrackPlayer.getRate()
}

async function getTrack(trackIndex: number): Promise<Track | null> {
  return TrackPlayer.getTrack(trackIndex)
}

async function getQueue(): Promise<Track[]> {
  return TrackPlayer.getQueue()
}

async function getCurrentTrack(): Promise<number> {
  return TrackPlayer.getCurrentTrack()
}

async function getDuration(): Promise<number> {
  return TrackPlayer.getDuration()
}

async function getBufferedPosition(): Promise<number> {
  return TrackPlayer.getBufferedPosition()
}

async function getPosition(): Promise<number> {
  return TrackPlayer.getPosition()
}

async function getState(): Promise<State> {
  return TrackPlayer.getState()
}

async function getRepeatMode(): Promise<RepeatMode> {
  return TrackPlayer.getRepeatMode()
}

export default {
  // MARK: - General API
  setupPlayer,
  destroy,
  registerPlaybackService,
  addEventListener,

  // MARK: - Queue API
  add,
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
  reset,
  play,
  pause,
  stop,
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
  getState,
  getRepeatMode,
}
