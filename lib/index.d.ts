import { MetadataOptions, PlayerOptions, Event, Track, State, TrackMetadata } from './interfaces';
declare function setupPlayer(options?: PlayerOptions): Promise<void>;
declare function destroy(): any;
declare function updateOptions(options?: MetadataOptions): Promise<void>;
declare type ServiceHandler = () => Promise<void>;
declare function registerPlaybackService(factory: () => ServiceHandler): void;
declare function addEventListener(event: Event, listener: (data: any) => void): import("react-native").EmitterSubscription;
declare function add(tracks: Track | Track[], insertBeforeId?: string): Promise<void>;
declare function addByIndex(tracks: Track | Track[], insertBeforeIndex?: number): Promise<void>;
declare function remove(tracks: Track | Track[]): Promise<void>;
declare function removeByIndex(tracks: Track | Track[]): Promise<void>;
declare function updateTrack(id: String, track: Track): Promise<void>;
declare function updateTrackbyIndex(index: number, track: Track): Promise<void>;
declare function removeUpcomingTracks(): Promise<void>;
declare function shuffle(): Promise<void>;
declare function shuffleFromIndex(index: number): Promise<void>;
declare function move(index: number, newIndex: number): Promise<void>;
declare function skip(trackId: string): Promise<void>;
declare function skipbyIndex(index: number): Promise<void>;
declare function skipToNext(): Promise<void>;
declare function skipToPrevious(): Promise<void>;
declare function updateMetadataForTrack(trackId: string, metadata: TrackMetadata): Promise<void>;
declare function updateMetadataForTrackByIndex(index: number, metadata: TrackMetadata): Promise<void>;
declare function reset(): Promise<void>;
declare function play(): Promise<void>;
declare function pause(): Promise<void>;
declare function stop(): Promise<void>;
declare function seekTo(position: number): Promise<void>;
declare function setVolume(level: number): Promise<void>;
declare function setRate(rate: number): Promise<void>;
declare function getVolume(): Promise<number>;
declare function getRate(): Promise<number>;
declare function getTrack(trackId: string): Promise<Track>;
declare function getTrackByIndex(index: number): Promise<Track>;
declare function getCachedStatus(key: string, length: number): Promise<number>;
declare function download(key: string, length: number, path: string, forceOverWrite: boolean): Promise<number>;
declare function getQueue(): Promise<Track[]>;
declare function getCurrentTrack(): Promise<string>;
declare function getDuration(): Promise<number>;
declare function getBufferedPosition(): Promise<number>;
declare function getPosition(): Promise<number>;
declare function getState(): Promise<State>;
declare function setRepeatMode(mode: number): Promise<void>;
declare function getRepeatMode(): Promise<State>;

export * from './hooks';
export * from './interfaces';
declare const _default: {
    setupPlayer: typeof setupPlayer;
    destroy: typeof destroy;
    updateOptions: typeof updateOptions;
    registerPlaybackService: typeof registerPlaybackService;
    addEventListener: typeof addEventListener;
    add: typeof add;
    remove: typeof remove;
    updateTrack: typeof updateTrack;
    removeUpcomingTracks: typeof removeUpcomingTracks;
    skip: typeof skip;
    skipToNext: typeof skipToNext;
    skipToPrevious: typeof skipToPrevious;
    updateMetadataForTrack: typeof updateMetadataForTrack;
    reset: typeof reset;
    play: typeof play;
    pause: typeof pause;
    stop: typeof stop;
    seekTo: typeof seekTo;
    setVolume: typeof setVolume;
    setRate: typeof setRate;
    getVolume: typeof getVolume;
    getRate: typeof getRate;
    getTrack: typeof getTrack;
    getQueue: typeof getQueue;
    getCurrentTrack: typeof getCurrentTrack;
    getDuration: typeof getDuration;
    getBufferedPosition: typeof getBufferedPosition;
    getPosition: typeof getPosition;
    getState: typeof getState;
    addByIndex: typeof addByIndex;
    removeByIndex: typeof removeByIndex;
    updateTrackbyIndex: typeof updateTrackbyIndex;
    shuffle: typeof shuffle;
    shuffleFromIndex: typeof shuffleFromIndex;
    move: typeof move;
    skipbyIndex: typeof skipbyIndex;
    updateMetadataForTrackByIndex: typeof updateMetadataForTrackByIndex;
    setRepeatMode: typeof setRepeatMode;
    getRepeatMode: typeof getRepeatMode;
};
export default _default;
