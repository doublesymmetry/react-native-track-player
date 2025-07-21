import { type TurboModule, TurboModuleRegistry } from 'react-native';
import { type UnsafeObject } from 'react-native/Libraries/Types/CodegenTypes';

export interface Spec extends TurboModule {
  // init and config
  setupPlayer(options: UnsafeObject): Promise<void>;
  updateOptions(options: UnsafeObject): Promise<void>;

  // player api
  load(track: UnsafeObject): Promise<number | void>;
  reset(): Promise<void>;
  play(): Promise<void>;
  pause(): Promise<void>;
  stop(): Promise<void>;
  setPlayWhenReady(playWhenReady: boolean): Promise<boolean>;
  getPlayWhenReady(): Promise<boolean>;
  seekTo(position: number): Promise<void>;
  seekBy(offset: number): Promise<void>;
  setVolume(level: number): Promise<void>;
  getVolume(): Promise<number>;
  setRate(rate: number): Promise<void>;
  getRate(): Promise<number>;
  getProgress(): Promise<UnsafeObject>;
  getPlaybackState(): Promise<UnsafeObject>;
  retry(): Promise<void>;

  // playlist management
  add(
    tracks: UnsafeObject[],
    insertBeforeIndex?: number
  ): Promise<number | void>;
  move(fromIndex: number, toIndex: number): Promise<void>;
  remove(indexes: number[]): Promise<void>;
  removeUpcomingTracks(): Promise<void>;
  skip(index: number, initialPosition?: number): Promise<void>;
  skipToNext(initialPosition?: number): Promise<void>;
  skipToPrevious(initialPosition?: number): Promise<void>;
  updateMetadataForTrack(
    trackIndex: number,
    metadata: UnsafeObject
  ): Promise<void>;
  updateNowPlayingMetadata(metadata: UnsafeObject): Promise<void>;
  setQueue(tracks: UnsafeObject[]): Promise<void>;
  getQueue(): Promise<UnsafeObject[]>;
  setRepeatMode(mode: number): Promise<number>;
  getRepeatMode(): Promise<number>;
  getTrack(index: number): Promise<UnsafeObject | undefined>;
  getActiveTrackIndex(): Promise<number | undefined>;
  getActiveTrack(): Promise<UnsafeObject | undefined>;

  // event listeners
  addListener(eventName: string): void;
  removeListeners(count: number): void;

  // constants
  getConstants: () => {
    // Capabilities
    CAPABILITY_PLAY: number;
    CAPABILITY_PLAY_FROM_ID: number;
    CAPABILITY_PLAY_FROM_SEARCH: number;
    CAPABILITY_PAUSE: number;
    CAPABILITY_STOP: number;
    CAPABILITY_SEEK_TO: number;
    CAPABILITY_SKIP: number;
    CAPABILITY_SKIP_TO_NEXT: number;
    CAPABILITY_SKIP_TO_PREVIOUS: number;
    CAPABILITY_SET_RATING: number;
    CAPABILITY_JUMP_FORWARD: number;
    CAPABILITY_JUMP_BACKWARD: number;

    // States
    STATE_NONE: string;
    STATE_READY: string;
    STATE_PLAYING: string;
    STATE_PAUSED: string;
    STATE_STOPPED: string;
    STATE_BUFFERING: string;
    STATE_LOADING: string;

    // Rating Types
    RATING_HEART: number;
    RATING_THUMBS_UP_DOWN: number;
    RATING_3_STARS: number;
    RATING_4_STARS: number;
    RATING_5_STARS: number;
    RATING_PERCENTAGE: number;

    // Repeat Modes
    REPEAT_OFF: number;
    REPEAT_TRACK: number;
    REPEAT_QUEUE: number;

    // Pitch Algorithms - iOS
    PITCH_ALGORITHM_LINEAR: number;
    PITCH_ALGORITHM_MUSIC: number;
    PITCH_ALGORITHM_VOICE: number;
  };

  // android methods
  acquireWakeLock(): Promise<void>;
  abandonWakeLock(): Promise<void>;
  validateOnStartCommandIntent(): Promise<boolean>;
}

const module = TurboModuleRegistry.getEnforcing<Spec>('TrackPlayer');
export const Constants = module?.getConstants();
export default module;
