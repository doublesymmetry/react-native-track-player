import { Platform, AppRegistry, DeviceEventEmitter, NativeEventEmitter } from 'react-native';
import TrackPlayer from './TrackPlayer';
import { TrackPlayerEvents } from './eventTypes';
import { ProgressComponent } from './ProgressComponent';
import { usePlaybackState, useTrackPlayerEvents, useTrackPlayerProgress } from './hooks';

const emitter = Platform.OS !== 'android' ? new NativeEventEmitter(TrackPlayer) : DeviceEventEmitter;

const registerPlaybackService = serviceFactory => {
    if (Platform.OS === 'android') {
        // Registers the headless task
        AppRegistry.registerHeadlessTask('TrackPlayer', serviceFactory);
    } else {
        // Initializes and runs the service in the next tick
        setImmediate(serviceFactory());
    }
};

const addEventListener = (event, listener) => emitter.addListener(event, listener);
const warpEventResponse = (handler, event, payload) => handler({ type: event, ...{ additionalKeys: payload || {}} });

/**
 * @deprecated since version 1.0.1. Use addEventListener instead.
 */
const registerEventHandler = (handler) => {
    let events = [
        'playback-state',
        'playback-error',
        'playback-queue-ended',
        'playback-track-changed',

        'remote-play',
        'remote-pause',
        'remote-stop',
        'remote-next',
        'remote-previous',
        'remote-jump-forward',
        'remote-jump-backward',
        'remote-seek',
        'remote-duck',
    ];
  
    if (Platform.OS === 'android') {
        events.push('remote-skip', 'remote-set-rating', 'remote-play-id', 'remote-play-search');
    }

    registerPlaybackService(() => {
        return async function() {
            for (let i = 0; i < events.length; i++) {
                addEventListener(events[i], warpEventResponse.bind(null, handler, events[i]));
            }
        };
    });
};

export {
    // General
    registerEventHandler,
    registerPlaybackService,
    registerPlaybackService as registerBackendService,
    addEventListener,
    // Player Event Types
    TrackPlayerEvents,
    // Components
    ProgressComponent,
    // React Hooks (Requires React v16.8+ and React Native v0.59+)
    usePlaybackState,
    useTrackPlayerEvents,
    useTrackPlayerProgress,
};
