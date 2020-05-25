import { Component } from "react";

export = RNTrackPlayer;

declare namespace RNTrackPlayer {
  export type EventType =
    | "playback-state"
    | "playback-error"
    | "playback-queue-ended"
    | "playback-track-changed"
    | "playback-metadata-received"
    | "remote-play"
    | "remote-play-id"
    | "remote-play-search"
    | "remote-pause"
    | "remote-stop"
    | "remote-skip"
    | "remote-next"
    | "remote-previous"
    | "remote-jump-forward"
    | "remote-jump-backward"
    | "remote-seek"
    | "remote-set-rating"
    | "remote-duck"
    | "remote-like"
    | "remote-dislike"
    | "remote-bookmark";

  export type TrackType = "default" | "dash" | "hls" | "smoothstreaming";

  type ResourceObject = any;

  type State =
    | typeof STATE_NONE
    | typeof STATE_PLAYING
    | typeof STATE_PAUSED
    | typeof STATE_STOPPED
    | typeof STATE_CONNECTING
    | typeof STATE_BUFFERING
    | typeof STATE_READY;
  type RatingType = string | number;
  type Capability = string | number;
  type PitchAlgorithm =
    | typeof PITCH_ALGORITHM_LINEAR_IOS
    | typeof PITCH_ALGORITHM_MUSIC_IOS
    | typeof PITCH_ALGORITHM_VOICE_IOS
    | typeof PITCH_ALGORITHM_ANDROID;

  type EventHandler = (type: EventType, ...args: any[]) => void;
  export function registerEventHandler(handler: EventHandler): void;

  type ServiceHandler = () => Promise<void>;
  export function registerPlaybackService(
    serviceFactory: () => ServiceHandler
  ): void;

  type EmitterSubscription = { remove: () => void };
  export function addEventListener(
    type: EventType,
    listener: (data: any) => void
  ): EmitterSubscription;

  export interface TrackMetadata {
    duration?: number;
    title: string;
    artist: string;
    album?: string;
    description?: string;
    genre?: string;
    date?: string;
    rating?: number | boolean;
    artwork?: string | ResourceObject;
  }

  export interface Track extends TrackMetadata {
    id: string;
    url: string | ResourceObject;
    type?: TrackType;
    userAgent?: string;
    contentType?: string;
    pitchAlgorithm?: PitchAlgorithm;
    [key: string]: any;
  }

  export interface PlayerOptions {
    minBuffer?: number;
    maxBuffer?: number;
    playBuffer?: number;
    backBuffer?: number;
    maxCacheSize?: number;
    iosCategory?:
      | "playback"
      | "playAndRecord"
      | "multiRoute"
      | "ambient"
      | "soloAmbient"
      | "record";
    iosCategoryMode?:
      | "default"
      | "gameChat"
      | "measurement"
      | "moviePlayback"
      | "spokenAudio"
      | "videoChat"
      | "videoRecording"
      | "voiceChat"
      | "voicePrompt";
    iosCategoryOptions?: Array<
      | "mixWithOthers"
      | "duckOthers"
      | "interruptSpokenAudioAndMixWithOthers"
      | "allowBluetooth"
      | "allowBluetoothA2DP"
      | "allowAirPlay"
      | "defaultToSpeaker"
    >;
    waitForBuffer?: boolean;
  }

  interface FeedbackOptions {
    /** Marks wether the option should be marked as active or "done" */
    isActive: boolean;

    /** The title to give the action (relevant for iOS) */
    title: string;
  }

  export interface MetadataOptions {
    ratingType?: RatingType;
    jumpInterval?: number;
    likeOptions?: FeedbackOptions;
    dislikeOptions?: FeedbackOptions;
    bookmarkOptions?: FeedbackOptions;
    stopWithApp?: boolean;
    alwaysPauseOnInterruption?: boolean; // default: false

    capabilities?: Capability[];
    notificationCapabilities?: Capability[];
    compactCapabilities?: Capability[];

    icon?: ResourceObject;
    playIcon?: ResourceObject;
    pauseIcon?: ResourceObject;
    stopIcon?: ResourceObject;
    previousIcon?: ResourceObject;
    nextIcon?: ResourceObject;
    rewindIcon?: ResourceObject;
    forwardIcon?: ResourceObject;
    color?: number;
  }

  // General

  export function setupPlayer(options?: PlayerOptions): Promise<void>;
  export function destroy(): void;

  // Player Queue Commands

  export function add(
    tracks: Track | Track[],
    insertBeforeId?: string
  ): Promise<void>;
  export function remove(trackIds: string | string[]): Promise<void>;
  export function skip(trackId: string): Promise<void>;
  export function skipToNext(): Promise<void>;
  export function skipToPrevious(): Promise<void>;
  export function removeUpcomingTracks(): Promise<void>;

  // Control Center / Notification Metadata Commands
  export function updateOptions(options: MetadataOptions): Promise<void>;
  export function updateMetadataForTrack(
    id: string,
    metadata: TrackMetadata
  ): Promise<void>;

  // Player Playback Commands

  export function reset(): Promise<void>;
  export function play(): Promise<void>;
  export function pause(): Promise<void>;
  export function stop(): Promise<void>;
  export function seekTo(seconds: number): Promise<void>;
  export function setVolume(level: number): Promise<void>;
  export function setRate(rate: number): Promise<void>;

  // Player Getters

  export function getQueue(): Promise<Track[]>;
  export function getTrack(id: string): Promise<Track>;
  export function getCurrentTrack(): Promise<string>;
  export function getVolume(): Promise<number>;
  export function getDuration(): Promise<number>;
  export function getPosition(): Promise<number>;
  export function getBufferedPosition(): Promise<number>;
  export function getState(): Promise<State>;
  export function getRate(): Promise<number>;

  // Components

  export interface ProgressComponentState {
    position: number;
    bufferedPosition: number;
    duration: number;
  }

  export class ProgressComponent<P = {}, S = {}> extends Component<
    P,
    ProgressComponentState & S
  > {
    public getProgress: () => number;
    public getBufferedProgress: () => number;
  }

  // Constants
  export const STATE_NONE: typeof STATE_ANDROID_NONE | typeof STATE_IOS_NONE;
  export const STATE_PLAYING:
    | typeof STATE_ANDROID_PLAYING
    | typeof STATE_IOS_PLAYING;
  export const STATE_PAUSED:
    | typeof STATE_ANDROID_PAUSED
    | typeof STATE_IOS_PAUSED;
  export const STATE_STOPPED:
    | typeof STATE_ANDROID_STOPPED
    | typeof STATE_IOS_STOPPED;
  export const STATE_CONNECTING:
    | typeof STATE_ANDROID_CONNECTING
    | typeof STATE_IOS_CONNECTING;
  export const STATE_BUFFERING:
    | typeof STATE_ANDROID_BUFFERING
    | typeof STATE_IOS_BUFFERING;
  export const STATE_READY:
    | typeof STATE_ANDROID_PAUSED
    | typeof STATE_IOS_READY;

  // Android states: See https://developer.android.com/reference/kotlin/android/support/v4/media/session/PlaybackStateCompat#STATE_CONNECTING:kotlin.Int
  export const STATE_ANDROID_NONE: 0;
  export const STATE_ANDROID_STOPPED: 1;
  export const STATE_ANDROID_PAUSED: 2;
  export const STATE_ANDROID_PLAYING: 3;
  export const STATE_ANDROID_FAST_FORWARDING: 4;
  export const STATE_ANDROID_REWINDING: 5;
  export const STATE_ANDROID_BUFFERING: 6;
  export const STATE_ANDROID_ERROR: 7;
  export const STATE_ANDROID_CONNECTING: 8;
  export const STATE_ANDROID_SKIPPING_TO_PREVIOUS: 9;
  export const STATE_ANDROID_SKIPPING_TO_NEXT: 10;
  export const STATE_ANDROID_SKIPPING_TO_QUEUE_ITEM: 11;

  // iOS states
  export const STATE_IOS_NONE: "idle";
  export const STATE_IOS_PLAYING: "playing";
  export const STATE_IOS_PAUSED: "paused";
  export const STATE_IOS_STOPPED: "idle";
  export const STATE_IOS_CONNECTING: undefined;
  export const STATE_IOS_BUFFERING: "loading";
  export const STATE_IOS_READY: "ready";

  export const RATING_HEART: RatingType;
  export const RATING_THUMBS_UP_DOWN: RatingType;
  export const RATING_3_STARS: RatingType;
  export const RATING_4_STARS: RatingType;
  export const RATING_5_STARS: RatingType;
  export const RATING_PERCENTAGE: RatingType;

  export const CAPABILITY_PLAY: Capability;
  export const CAPABILITY_PLAY_FROM_ID: Capability;
  export const CAPABILITY_PLAY_FROM_SEARCH: Capability;
  export const CAPABILITY_PAUSE: Capability;
  export const CAPABILITY_STOP: Capability;
  export const CAPABILITY_SEEK_TO: Capability;
  export const CAPABILITY_SKIP: Capability;
  export const CAPABILITY_SKIP_TO_NEXT: Capability;
  export const CAPABILITY_SKIP_TO_PREVIOUS: Capability;
  export const CAPABILITY_SET_RATING: Capability;
  export const CAPABILITY_JUMP_FORWARD: Capability;
  export const CAPABILITY_JUMP_BACKWARD: Capability;
  export const CAPABILITY_LIKE: Capability;
  export const CAPABILITY_DISLIKE: Capability;
  export const CAPABILITY_BOOKMARK: Capability;

  export const PITCH_ALGORITHM_LINEAR: typeof PITCH_ALGORITHM_LINEAR_IOS | typeof PITCH_ALGORITHM_ANDROID;
  export const PITCH_ALGORITHM_MUSIC: typeof PITCH_ALGORITHM_MUSIC_IOS | typeof PITCH_ALGORITHM_ANDROID;
  export const PITCH_ALGORITHM_VOICE: typeof PITCH_ALGORITHM_VOICE_IOS | typeof PITCH_ALGORITHM_ANDROID;

  export const PITCH_ALGORITHM_LINEAR_IOS: "linear";
  export const PITCH_ALGORITHM_MUSIC_IOS: "music";
  export const PITCH_ALGORITHM_VOICE_IOS: "voice";

  export const PITCH_ALGORITHM_ANDROID: undefined;

  export const TrackPlayerEvents: {
    REMOTE_PLAY: EventType;
    REMOTE_PLAY_ID: EventType;
    REMOTE_PLAY_SEARCH: EventType;
    REMOTE_PAUSE: EventType;
    REMOTE_STOP: EventType;
    REMOTE_SKIP: EventType;
    REMOTE_NEXT: EventType;
    REMOTE_PREVIOUS: EventType;
    REMOTE_SEEK: EventType;
    REMOTE_SET_RATING: EventType;
    REMOTE_JUMP_FORWARD: EventType;
    REMOTE_JUMP_BACKWARD: EventType;
    REMOTE_DUCK: EventType;
    REMOTE_LIKE: EventType;
    REMOTE_DISLIKE: EventType;
    REMOTE_BOOKMARK: EventType;
    PLAYBACK_STATE: EventType;
    PLAYBACK_TRACK_CHANGED: EventType;
    PLAYBACK_QUEUE_ENDED: EventType;
    PLAYBACK_ERROR: EventType;
    PLAYBACK_METADATA_RECEIVED: EventType;
  };

  // Hooks
  export function usePlaybackState(): State;
  export function useTrackPlayerEvents(
    events: string[],
    handler: (event: any) => void
  ): void;
  export function useInterval(callback: () => void, delay: number): void;
  export function useWhenPlaybackStateChanges(callback: () => void): void;
  export function usePlaybackStateIs(...states: State[]): boolean;
  export function useTrackPlayerProgress(
    interval?: number
  ): ProgressComponentState;
}
