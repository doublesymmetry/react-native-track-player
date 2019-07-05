import { Platform, AppRegistry, DeviceEventEmitter, NativeEventEmitter, NativeModules } from 'react-native'
// @ts-ignore
import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource'

import {
  MetadataOptions,
  PlayerOptions,
  Event,
  Track,
  State,
  TrackMetadata,
  Capability,
  PitchAlgorithm,
  RatingType,
} from './interfaces'

const { TrackPlayerModule: TrackPlayer } = NativeModules
const emitter = Platform.OS !== 'android' ? new NativeEventEmitter(TrackPlayer) : DeviceEventEmitter

export { useTrackPlayerProgress, usePlaybackState, useTrackPlayerEvents } from './hooks'
export { MetadataOptions, PlayerOptions, Event, Track, State, TrackMetadata, Capability, PitchAlgorithm, RatingType }

// MARK: - Helpers

function resolveImportedPath(path?: number | string) {
  if (!path) return undefined
  return resolveAssetSource(path) || path
}

// MARK: - General API

export async function setupPlayer(options: PlayerOptions = {}): Promise<void> {
  return TrackPlayer.setupPlayer(options || {})
}

export function destroy() {
  return TrackPlayer.destroy()
}

export async function updateOptions(options: MetadataOptions = {}): Promise<void> {
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

type ServiceHandler = () => Promise<void>
export function registerPlaybackService(factory: () => ServiceHandler) {
  if (Platform.OS === 'android') {
    // Registers the headless task
    AppRegistry.registerHeadlessTask('TrackPlayer', factory)
  } else {
    // Initializes and runs the service in the next tick
    setImmediate(factory())
  }
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function addEventListener(event: Event, listener: (data: any) => void) {
  return emitter.addListener(event, listener)
}

// MARK: - Queue API

export async function add(tracks: Track | Track[], insertBeforeId?: string): Promise<void> {
  if (!Array.isArray(tracks)) {
    tracks = [tracks]
  }

  if (tracks.length < 1) return

  for (let i = 0; i < tracks.length; i++) {
    // Clone the object before modifying it
    tracks[i] = { ...tracks[i] }

    // Resolve the URLs
    tracks[i].url = resolveImportedPath(tracks[i].url)
    tracks[i].artwork = resolveImportedPath(tracks[i].artwork)

    // Cast ID's into strings
    tracks[i].id = `${tracks[i].id}`
  }

  return TrackPlayer.add(tracks, insertBeforeId)
}

export async function remove(tracks: Track | Track[]): Promise<void> {
  if (!Array.isArray(tracks)) {
    tracks = [tracks]
  }

  return TrackPlayer.remove(tracks)
}

export async function removeUpcomingTracks(): Promise<void> {
  return TrackPlayer.removeUpcomingTracks()
}

export async function skip(trackId: string): Promise<void> {
  return TrackPlayer.skip(trackId)
}

export async function skipToNext(): Promise<void> {
  return TrackPlayer.skipToNext()
}

export async function skipToPrevious(): Promise<void> {
  return TrackPlayer.skipToPrevious()
}

export async function updateMetadataForTrack(trackId: string, metadata: TrackMetadata): Promise<void> {
  return TrackPlayer.updateMetadataForTrack(trackId, metadata)
}

// MARK: Playback API

export async function reset(): Promise<void> {
  return TrackPlayer.reset()
}

export async function play(): Promise<void> {
  return TrackPlayer.play()
}

export async function pause(): Promise<void> {
  return TrackPlayer.pause()
}

export async function stop(): Promise<void> {
  return TrackPlayer.stop()
}

export async function seekTo(position: number): Promise<void> {
  return TrackPlayer.seekTo(position)
}

export async function setVolume(level: number): Promise<void> {
  return TrackPlayer.setVolume(level)
}

export async function setRate(rate: number): Promise<void> {
  return TrackPlayer.setRate(rate)
}

// MARK: - Getters

export async function getVolume(): Promise<number> {
  return TrackPlayer.getVolume()
}

export async function getRate(): Promise<number> {
  return TrackPlayer.getRate()
}

export async function getTrack(trackId: string): Promise<Track> {
  return TrackPlayer.getRate(trackId)
}

export async function getQueue(): Promise<Track[]> {
  return TrackPlayer.getQueue()
}

export async function getCurrentTrack(): Promise<string> {
  return TrackPlayer.getCurrentTrack()
}

export async function getDuration(): Promise<number> {
  return TrackPlayer.getDuration()
}

export async function getBufferedPosition(): Promise<number> {
  return TrackPlayer.getBufferedPosition()
}

export async function getPosition(): Promise<number> {
  return TrackPlayer.getPosition()
}

export async function getState(): Promise<State> {
  return TrackPlayer.getState()
}

// MARK: - State Constants

export const STATE_NONE: State = TrackPlayer.STATE_NONE
export const STATE_READY: State = TrackPlayer.STATE_READY
export const STATE_PLAYING: State = TrackPlayer.STATE_PLAYING
export const STATE_PAUSED: State = TrackPlayer.STATE_PAUSED
export const STATE_STOPPED: State = TrackPlayer.STATE_STOPPED
export const STATE_BUFFERING: State = TrackPlayer.STATE_BUFFERING
export const STATE_CONNECTING: State = TrackPlayer.STATE_CONNECTING

// MARK: - Capabilities Constants

export const CAPABILITY_PLAY: Capability = TrackPlayer.CAPABILITY_PLAY
export const CAPABILITY_PLAY_FROM_ID: Capability = TrackPlayer.CAPABILITY_PLAY_FROM_ID
export const CAPABILITY_PLAY_FROM_SEARCH: Capability = TrackPlayer.CAPABILITY_PLAY_FROM_SEARCH
export const CAPABILITY_PAUSE: Capability = TrackPlayer.CAPABILITY_PAUSE
export const CAPABILITY_STOP: Capability = TrackPlayer.CAPABILITY_STOP
export const CAPABILITY_SEEK_TO: Capability = TrackPlayer.CAPABILITY_SEEK_TO
export const CAPABILITY_SKIP: Capability = TrackPlayer.CAPABILITY_SKIP
export const CAPABILITY_SKIP_TO_NEXT: Capability = TrackPlayer.CAPABILITY_SKIP_TO_NEXT
export const CAPABILITY_SKIP_TO_PREVIOUS: Capability = TrackPlayer.CAPABILITY_SKIP_TO_PREVIOUS
export const CAPABILITY_JUMP_FORWARD: Capability = TrackPlayer.CAPABILITY_JUMP_FORWARD
export const CAPABILITY_JUMP_BACKWARD: Capability = TrackPlayer.CAPABILITY_JUMP_BACKWARD
export const CAPABILITY_SET_RATING: Capability = TrackPlayer.CAPABILITY_SET_RATING
export const CAPABILITY_LIKE: Capability = TrackPlayer.CAPABILITY_LIKE
export const CAPABILITY_DISLIKE: Capability = TrackPlayer.CAPABILITY_DISLIKE
export const CAPABILITY_BOOKMARK: Capability = TrackPlayer.CAPABILITY_BOOKMARK

// MARK: - Pitch Constants
export const PITCH_ALGORITHM_LINEAR: PitchAlgorithm = TrackPlayer.PITCH_ALGORITHM_LINEAR
export const PITCH_ALGORITHM_MUSIC: PitchAlgorithm = TrackPlayer.PITCH_ALGORITHM_MUSIC
export const PITCH_ALGORITHM_VOICE: PitchAlgorithm = TrackPlayer.PITCH_ALGORITHM_VOICE

// MARK: - Rating Constants
export const RATING_HEART: RatingType = TrackPlayer.RATING_HEART
export const RATING_THUMBS_UP_DOWN: RatingType = TrackPlayer.RATING_THUMBS_UP_DOWN
export const RATING_3_STARS: RatingType = TrackPlayer.RATING_3_STARS
export const RATING_4_STARS: RatingType = TrackPlayer.RATING_4_STARS
export const RATING_5_STARS: RatingType = TrackPlayer.RATING_5_STARS
export const RATING_PERCENTAGE: RatingType = TrackPlayer.RATING_PERCENTAGE
