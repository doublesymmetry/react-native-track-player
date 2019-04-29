import { useEffect } from 'react';
import * as TrackPlayer from './index';
import TrackPlayerEvents from './eventTypes';

function isValidTrackPlayerEvent(evt) {
    return TrackPlayerEvents[evt] !== undefined;
}

/**
 * @description
 *   Attaches handler to TrackPlayer event and performs cleanup on unmount
 */
module.exports.useTrackPlayerEvent = (event, handler) => {
    useEffect(
        () => {
            if (!isValidTrackPlayerEvent(event)) {
                return;
            }

            const sub = TrackPlayer.addEventListener(evt, handler);

            return () => {
                sub.remove();
            }
        },
        [event, handler]
    )
}