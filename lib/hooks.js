import { useEffect, useState, useDebugValue, useRef } from 'react';
import TrackPlayer from './trackPlayer';
import TrackPlayerEvents from './eventTypes';

/**
 * @description
 *   Get current playback state and subsequent updates
 */
const usePlaybackState = () => {
    const [state, setState] = useState(TrackPlayer.STATE_NONE)

    useEffect(() => {
        async function setPlayerState() {
            const playerState = await TrackPlayer.getState()
            setState(playerState)
        }

        setPlayerState()

        const sub = TrackPlayer.addEventListener(TrackPlayerEvents.PLAYBACK_STATE, data => {
            setState(data.state)
        })

        return () => {
            sub.remove()
        }
    }, [])

    return state
}

/**
 * @description
 *   Attaches a handler to the given TrackPlayer events and performs cleanup on unmount
 * @param {Array<string>} events - TrackPlayer events to subscribe to
 * @param {(payload: any) => void} handler - callback invoked when the event fires
 */
const useTrackPlayerEvents = (events, handler) => {
    const savedHandler = useRef();

    useEffect(() => {
        savedHandler.current = handler;
    }, [handler]);

    useEffect(
        () => {
            if (__DEV__) {
                const allowedTypes = Object.values(TrackPlayerEvents);
                const invalidTypes = events.filter(type => !allowedTypes.includes(type));
                if (invalidTypes.length) {
                    console.warn(
                        'One or more of the events provided to useTrackPlayerEvents is ' +
                        `not a valid TrackPlayer event: ${invalidTypes.join('\', \'')}. ` +
                        'A list of available events can be found at ' +
                        'https://react-native-kit.github.io/react-native-track-player/documentation/#events'
                    )
                }
            }

            const subs = events.map(event =>
                TrackPlayer.addEventListener(event,
                    (payload) => savedHandler.current({ ...payload, type: event })
                )
            );

            return () => {
                subs.forEach(sub => sub.remove());
            }
        },
        events
    );
}

const useInterval = (callback, delay) => {
    const savedCallback = useRef();

    useEffect(() => {
        savedCallback.current = callback;
    })

    useEffect(() => {
        if (!delay) return;
        savedCallback.current();
        const id = setInterval(savedCallback.current, delay);
        return () => clearInterval(id);
    }, [delay]);
}

const useWhenPlaybackStateChanges = callback => {
    useTrackPlayerEvents(
        [TrackPlayerEvents.PLAYBACK_STATE],
        ({ state }) => {
            callback(state);
        }
    );
    useEffect(() => {
        let didCancel = false;
        const fetchPlaybackState = async () => {
            const playbackState = await TrackPlayer.getState();
            if (!didCancel) {
                callback(playbackState);
            }
        }
        fetchPlaybackState();
        return () => { didCancel = true };
    }, []);
}

const usePlaybackStateIs = (...states) => {
    const [is, setIs] = useState();
    useWhenPlaybackStateChanges(state => {
        setIs(states.includes(state));
    });

    return is;
}

/**
 * @description
 *   Poll for track progress for the given interval (in miliseconds)
 * @param {number} interval - ms interval
 * @param {Array<TrackPlayerState> | null} pollTrackPlayerStates - TrackPlayer states on which the track progress should be polled. If null is provided, the track progress will be polled every `interval` seconds.
 * @returns {[
 *   {
 *      progress: number,
 *      bufferedPosition: number,
 *      duration: number
 *   },
 *   (interval: number) => void
 * ]}
 */
const useTrackPlayerProgress = (
    interval = 1000,
    pollTrackPlayerStates = [
        TrackPlayer.STATE_PLAYING,
        TrackPlayer.STATE_BUFFERING,
    ]
) => {
    const initialState = {
        position: 0,
        bufferedPosition: 0,
        duration: 0,
    };

    const [state, setState] = useState(initialState);

    const getProgress = async () => {
        const [position, bufferedPosition, duration] = await Promise.all([
            TrackPlayer.getPosition(),
            TrackPlayer.getBufferedPosition(),
            TrackPlayer.getDuration(),
        ]);
        setState({ position, bufferedPosition, duration });
    };

    let needsPoll = true;
    if (pollTrackPlayerStates) {
        needsPoll = usePlaybackStateIs(...pollTrackPlayerStates);
    }

    useInterval(getProgress, needsPoll ? interval : null);
    return state;
};


// Exports
module.exports.usePlaybackState = usePlaybackState
module.exports.useTrackPlayerEvents = useTrackPlayerEvents
module.exports.useInterval = useInterval
module.exports.useWhenPlaybackStateChanges = useWhenPlaybackStateChanges
module.exports.usePlaybackStateIs = usePlaybackStateIs
module.exports.useTrackPlayerProgress = useTrackPlayerProgress
