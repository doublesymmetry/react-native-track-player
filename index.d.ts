import { NativeModules } from 'react-native';

export = RNTrackPlayer;
export as namespace RNTrackPlayer;

declare namespace RNTrackPlayer {

  // General

  export interface PlayerOptions {
    ratingType?: any;
    maxArtworkSize?: number;
    capabilities?: string[];
    compactCapabilities?: string[];

    icon?: number;
    playIcon?: number;
    pauseIcon?: number;
    stopIcon?: number;
    previousIcon?: number;
    nextIcon?: number;
    color?: number;

    stopWithApp?: boolean;
  }

  export function setupPlayer(options?: PlayerOptions): Promise<void>;
  export function destroy(): void;
  export function updateOptions(options?: PlayerOptions): Promise<void>;

  export type EventType = 'playback-state'
    | 'playback-error'
    | 'playback-queue-ended'
    | 'playback-track-changed'
    | 'remote-play'
    | 'remote-pause'
    | 'remote-stop'
    | 'remote-next'
    | 'remote-previous'
    | 'remote-jump-forward'
    | 'remote-jump-backward'
  
  type Handler = (type: EventType, ...args: any[]) => void;
  export function registerEventHandler(handler: Handler): void;


  // Player Queue Commands

  export interface Track {
    id: string;
    url: string|number;
    type?: string;
    contentType?: string;
    duration?: number;
    title: string;
    artist: string;
    album?: string;
    description?: string;
    genre?: string;
    date?: string;
    rating?: any;
    artwork?: string;
    sendUrl?: boolean;
    [key: string]: any;
  }

  export function add(tracks: Track|Track[], insertBeforeId?: string): Promise<void>;
  export function remove(trackIds: String|String[]): Promise<void>;
  export function skip(trackId: string): Promise<void>;
  export function getQueue(): Promise<Track[]>;
  export function skipToNext(): Promise<void>;
  export function skipToPrevious(): Promise<void>;
  export function removeUpcomingTracks(): Promise<void>;


  // Player Playback Commands

  export function reset(): Promise<void>;
  export function play(): Promise<void>;
  export function pause(): Promise<void>;
  export function stop(): Promise<void>;
  export function seekTo(time: number): Promise<void>;
  export function setVolume(level: number): Promise<void>;
  export function setRate(rate: number): Promise<void>;


  // Player Getters

  export function getTrack(id: string): Promise<Track>;
  export function getCurrentTrack(): Promise<string>;
  export function getVolume(): Promise<number>;
  export function getDuration(): Promise<number>;
  export function getPosition(): Promise<number>;
  export function getBufferedPosition(): Promise<number>;
  export function getState(): Promise<string>;
  export function getRate(): Promise<number>;
}
