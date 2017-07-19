import { NativeModules } from 'react-native';
import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource';

import CastButton from './CastButton.js';

const TrackPlayer = NativeModules.TrackPlayerModule;

function resolveAsset(uri) {
    if(!uri) return undefined;
    return resolveAssetSource(uri);
}

function resolveUrl(url) {
    if(!url) return undefined;
    return resolveAssetSource(url) || url;
}

function setOptions(data) {
    data.icon = resolveAsset(data.icon);
    data.playIcon = resolveAsset(data.playIcon);
    data.pauseIcon = resolveAsset(data.pauseIcon);
    data.stopIcon = resolveAsset(data.stopIcon);
    data.previousIcon = resolveAsset(data.previousIcon);
    data.nextIcon = resolveAsset(data.nextIcon);

    return TrackPlayer.setOptions(data);
}

function add(insertBeforeId, tracks) {
    for(let i = 0; i < tracks.length; i++) {
        tracks[i].url = resolveUrl(tracks[i].url);
        tracks[i].artwork = resolveUrl(tracks[i].artwork);
    }

    return TrackPlayer.add(insertBeforeId, tracks);
}

function load(track) {
    track.url = resolveUrl(track.url);
    track.artwork = resolveUrl(track.url);

    return TrackPlayer.load(track);
}

// We'll declare each one of the constants and functions manually
// so IDEs can show a list of functions and constants to the app dev
// We can also add documentation here, but I'll leave this task to another day

// States
module.exports.STATE_NONE = TrackPlayer.STATE_NONE;
module.exports.STATE_PLAYING = TrackPlayer.STATE_PLAYING;
module.exports.STATE_PAUSED = TrackPlayer.STATE_PAUSED;
module.exports.STATE_STOPPED = TrackPlayer.STATE_STOPPED;
module.exports.STATE_BUFFERING = TrackPlayer.STATE_BUFFERING;

// Capabilities
module.exports.CAPABILITY_PLAY = TrackPlayer.CAPABILITY_PLAY;
module.exports.CAPABILITY_PLAY_FROM_ID = TrackPlayer.CAPABILITY_PLAY_FROM_ID;
module.exports.CAPABILITY_PLAY_FROM_SEARCH = TrackPlayer.CAPABILITY_PLAY_FROM_SEARCH;
module.exports.CAPABILITY_PAUSE = TrackPlayer.CAPABILITY_PAUSE;
module.exports.CAPABILITY_STOP = TrackPlayer.CAPABILITY_STOP;
module.exports.CAPABILITY_SEEK_TO = TrackPlayer.CAPABILITY_SEEK_TO;
module.exports.CAPABILITY_SKIP = TrackPlayer.CAPABILITY_SKIP;
module.exports.CAPABILITY_SKIP_TO_NEXT = TrackPlayer.CAPABILITY_SKIP_TO_NEXT;
module.exports.CAPABILITY_SKIP_TO_PREVIOUS = TrackPlayer.CAPABILITY_SKIP_TO_PREVIOUS;
module.exports.CAPABILITY_SET_RATING = TrackPlayer.CAPABILITY_SET_RATING;

// Rating Types
module.exports.RATING_HEART = TrackPlayer.RATING_HEART;
module.exports.RATING_THUMBS_UP_DOWN = TrackPlayer.RATING_THUMBS_UP_DOWN;
module.exports.RATING_3_STARS = TrackPlayer.RATING_3_STARS;
module.exports.RATING_4_STARS = TrackPlayer.RATING_4_STARS;
module.exports.RATING_5_STARS = TrackPlayer.RATING_5_STARS;
module.exports.RATING_PERCENTAGE = TrackPlayer.RATING_PERCENTAGE;

// Cast States
module.exports.CAST_NO_DEVICES_AVAILABLE = TrackPlayer.CAST_NO_DEVICES_AVAILABLE;
module.exports.CAST_NOT_CONNECTED = TrackPlayer.CAST_NOT_CONNECTED;
module.exports.CAST_CONNECTING = TrackPlayer.CAST_CONNECTING;
module.exports.CAST_CONNECTED = TrackPlayer.CAST_CONNECTED;

// Basics
module.exports.onReady = TrackPlayer.onReady;
module.exports.setOptions = setOptions;

// Player Basics
module.exports.setupPlayer = TrackPlayer.setupPlayer;
module.exports.destroy = TrackPlayer.destroy;

// Player Queue Commands
module.exports.add = add;
module.exports.remove = TrackPlayer.remove;
module.exports.skip = TrackPlayer.skip;
module.exports.skipToNext = TrackPlayer.skipToNext;
module.exports.skipToPrevious = TrackPlayer.skipToPrevious;

// Player Playback Commands
module.exports.reset = TrackPlayer.reset;
module.exports.load = load;
module.exports.play = TrackPlayer.play;
module.exports.pause = TrackPlayer.pause;
module.exports.stop = TrackPlayer.stop;
module.exports.seekTo = TrackPlayer.seekTo;
module.exports.setVolume = TrackPlayer.setVolume;

// Player Getters
module.exports.getTrack = TrackPlayer.getTrack;
module.exports.getCurrentTrack = TrackPlayer.getCurrentTrack;
module.exports.getVolume = TrackPlayer.getVolume;
module.exports.getDuration = TrackPlayer.getDuration;
module.exports.getPosition = TrackPlayer.getPosition;
module.exports.getBufferedPosition = TrackPlayer.getBufferedPosition;
module.exports.getState = TrackPlayer.getState;

// Components
module.exports.CastButton = CastButton;
