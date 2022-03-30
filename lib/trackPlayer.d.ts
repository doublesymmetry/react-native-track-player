import { MetadataOptions, PlayerOptions, Event, Track, State, TrackMetadataBase, NowPlayingMetadata, RepeatMode } from './interfaces';
/**
 * Initializes the player with the specified options.
 */
declare function setupPlayer(options?: PlayerOptions): Promise<void>;
/**
 * Destroys the player, cleaning up its resources.
 */
declare function destroy(): any;
declare type ServiceHandler = () => Promise<void>;
/**
 * Register the playback service. The service will run as long as the player runs.
 */
declare function registerPlaybackService(factory: () => ServiceHandler): void;
declare function addEventListener(event: Event, listener: (data: any) => void): import("react-native").EmitterSubscription;
declare function isServiceRunning(): Promise<boolean>;
/**
 * Adds one or more tracks to the queue.
 */
declare function add(tracks: Track | Track[], insertBeforeIndex?: number): Promise<void>;
/**
 * Removes one or more tracks from the queue.
 */
declare function remove(tracks: number | number[]): Promise<void>;
/**
 * Clears any upcoming tracks from the queue.
 */
declare function removeUpcomingTracks(): Promise<void>;
/**
 * Skips to a track in the queue.
 */
declare function skip(trackIndex: number): Promise<void>;
/**
 * Skips to the next track in the queue.
 */
declare function skipToNext(): Promise<void>;
/**
 * Skips to the previous track in the queue.
 */
declare function skipToPrevious(): Promise<void>;
/**
 * Updates the configuration for the components.
 */
declare function updateOptions(options?: MetadataOptions): Promise<void>;
/**
 * Updates the metadata of a track in the queue. If the current track is updated,
 * the notification and the Now Playing Center will be updated accordingly.
 */
declare function updateMetadataForTrack(trackIndex: number, metadata: TrackMetadataBase): Promise<void>;
declare function clearNowPlayingMetadata(): Promise<void>;
declare function updateNowPlayingMetadata(metadata: NowPlayingMetadata): Promise<void>;
/**
 * Resets the player stopping the current track and clearing the queue.
 */
declare function reset(): Promise<void>;
/**
 * Plays or resumes the current track.
 */
declare function play(): Promise<void>;
/**
 * Pauses the current track.
 */
declare function pause(): Promise<void>;
/**
 * Stops the current track.
 */
declare function stop(): Promise<void>;
/**
 * Seeks to a specified time position in the current track.
 */
declare function seekTo(position: number): Promise<void>;
/**
 * Sets the volume of the player.
 */
declare function setVolume(level: number): Promise<void>;
/**
 * Sets the playback rate.
 */
declare function setRate(rate: number): Promise<void>;
/**
 * Sets the repeat mode.
 */
declare function setRepeatMode(mode: RepeatMode): Promise<RepeatMode>;
/**
 * Gets the volume of the player (a number between 0 and 1).
 */
declare function getVolume(): Promise<number>;
/**
 * Gets the playback rate, where 1 is the regular speed.
 */
declare function getRate(): Promise<number>;
/**
 * Gets a track object from the queue.
 */
declare function getTrack(trackIndex: number): Promise<Track | null>;
/**
 * Gets the whole queue.
 */
declare function getQueue(): Promise<Track[]>;
/**
 * Gets the index of the current track.
 */
declare function getCurrentTrack(): Promise<number>;
/**
 * Gets the duration of the current track in seconds.
 */
declare function getDuration(): Promise<number>;
/**
 * Gets the buffered position of the player in seconds.
 */
declare function getBufferedPosition(): Promise<number>;
/**
 * Gets the position of the player in seconds.
 */
declare function getPosition(): Promise<number>;
/**
 * Gets the state of the player.
 */
declare function getState(): Promise<State>;
/**
 * Gets the repeat mode.
 */
declare function getRepeatMode(): Promise<RepeatMode>;
/**
 * Sets whether to erase notifications
 */
declare function setEraseNotificationBehaviour(erase: boolean): Promise<void>;
declare const _default: {
    setupPlayer: typeof setupPlayer;
    destroy: typeof destroy;
    registerPlaybackService: typeof registerPlaybackService;
    addEventListener: typeof addEventListener;
    isServiceRunning: typeof isServiceRunning;
    add: typeof add;
    remove: typeof remove;
    removeUpcomingTracks: typeof removeUpcomingTracks;
    skip: typeof skip;
    skipToNext: typeof skipToNext;
    skipToPrevious: typeof skipToPrevious;
    updateOptions: typeof updateOptions;
    updateMetadataForTrack: typeof updateMetadataForTrack;
    clearNowPlayingMetadata: typeof clearNowPlayingMetadata;
    updateNowPlayingMetadata: typeof updateNowPlayingMetadata;
    setEraseNotificationBehaviour: typeof setEraseNotificationBehaviour;
    reset: typeof reset;
    play: typeof play;
    pause: typeof pause;
    stop: typeof stop;
    seekTo: typeof seekTo;
    setVolume: typeof setVolume;
    setRate: typeof setRate;
    setRepeatMode: typeof setRepeatMode;
    getVolume: typeof getVolume;
    getRate: typeof getRate;
    getTrack: typeof getTrack;
    getQueue: typeof getQueue;
    getCurrentTrack: typeof getCurrentTrack;
    getDuration: typeof getDuration;
    getBufferedPosition: typeof getBufferedPosition;
    getPosition: typeof getPosition;
    getState: typeof getState;
    getRepeatMode: typeof getRepeatMode;
};
export default _default;
