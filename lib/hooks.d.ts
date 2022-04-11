import { State, Event } from './interfaces';
/** Get current playback state and subsequent updatates  */
export declare const usePlaybackState: () => State;
declare type Handler = (payload: {
    type: Event;
    [key: string]: any;
}) => void;
/**
 * Attaches a handler to the given TrackPlayer events and performs cleanup on unmount
 * @param events - TrackPlayer events to subscribe to
 * @param handler - callback invoked when the event fires
 */
export declare const useTrackPlayerEvents: (events: Event[], handler: Handler) => void;
export interface ProgressState {
    position: number;
    duration: number;
    buffered: number;
}
/**
 * Poll for track progress for the given interval (in miliseconds)
 * @param interval - ms interval
 */
export declare function useProgress(updateInterval?: number): ProgressState;
export {};
