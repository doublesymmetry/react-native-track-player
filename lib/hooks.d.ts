import { Event } from './index';
/**
 * @description
 *   Get current playback state and subsequent updatates
 */
export declare const usePlaybackState: () => string | number;
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
/**
 * Poll for track progress for the given interval (in miliseconds)
 * @param interval - ms interval
 */
export declare const useTrackPlayerProgress: (interval?: number) => {
    position: number;
    bufferedPosition: number;
    duration: number;
};
export {};
