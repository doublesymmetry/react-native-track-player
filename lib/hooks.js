import { useEffect, useState, useDebugValue } from 'react';
import * as TrackPlayer from './index';
import TrackPlayerEvents from './eventTypes';

function isValidTrackPlayerEvent(evt) {
    return TrackPlayerEvents[evt] !== undefined;
}

/**
 * @description
 *   Attaches a handler to the given TrackPlayer events and performs cleanup on unmount
 * @param {Array<string>} events - TrackPlayer events to subscribe to
 * @param {(payload: any) => void} handler - callback invoked when the event fires
 */
module.exports.useTrackPlayerEvents = (events, handler) => {
    const savedHandler = useRef();

    useEffect(() => {
        savedHandler.current = handler;
    }, [handler]);

    useEffect(
        () => {
            const playerEvents = events.filter(isValidTrackPlayerEvent); 

            if (__DEV__) {
                if (events.length !== playerEvents.length) {
                    console.warn(
                        'One or more of the events provided to useTrackPlayerEvents is ' +
                        'not a valid TrackPlayer event. A list of available events can ' +
                        'be found at https://react-native-kit.github.io/react-native-track-player/documentation/#events'
                    )
                }
            }

            const subs = playerEvents.map(event => 
                TrackPlayer.addEventListener(evt, 
                    (payload) => savedHandler.current({ ...payload, type: event })
                )
            );

            return () => {
                subs.forEach(sub => sub.remove());
            }
        },
        [events]
    );
}

function useInterval(callback, delay) {
    const savedCallback = useRef();

    useEffect(() => {
        savedCallback.current = callback;
    })

    useEffect(() => {
        function tick() {
            savedCallback.current();
        }

        const id = setInterval(tick, delay);
        return () => clearInterval(id);
    }, [delay]);
}

/**
 * @description
 *   Poll for track progress for the given interval (in miliseconds)
 * @param {number} interval - ms interval
 * @returns {[
 *   { 
 *      progress: number,
 *      bufferedPosition: number,
 *      duration: number
 *   },
 *   (interval: number) => void
 * ]}
 */
module.exports.useTrackPlayerProgress = (interval = 1000) => {
    const initialState = {
        position: 0,
        bufferedPosition: 0,
        duration: 0 
    };

    const [state, setState] = useState(initialState);
    const [interval, setInterval] = useState(interval);

    async function getProgress() {
        try {
            const [position, bufferedPosition, duration] = await Promise.all([
                TrackPlayer.getPosition(),
                TrackPlayer.getBufferedPosition(),
                TrackPlayer.getDuration()
            ])

            setState({ position, bufferedPosition, duration });
        } catch (err) {
            // The player is probably not ready yet.
            // todo: better error handling
        }
    }

    useInterval(getProgress, interval);

    return [state, setInterval];
}
