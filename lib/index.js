"use strict";
var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (_) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
Object.defineProperty(exports, "__esModule", { value: true });
var react_native_1 = require("react-native");
// @ts-ignore
var resolveAssetSource_1 = require("react-native/Libraries/Image/resolveAssetSource");
var interfaces_1 = require("./interfaces");
exports.Event = interfaces_1.Event;
var TrackPlayer = react_native_1.NativeModules.TrackPlayerModule;
var emitter = react_native_1.Platform.OS !== 'android' ? new react_native_1.NativeEventEmitter(TrackPlayer) : react_native_1.DeviceEventEmitter;
var hooks_1 = require("./hooks");
exports.useTrackPlayerProgress = hooks_1.useTrackPlayerProgress;
exports.usePlaybackState = hooks_1.usePlaybackState;
exports.useTrackPlayerEvents = hooks_1.useTrackPlayerEvents;
// MARK: - Helpers
function resolveImportedPath(path) {
    if (!path)
        return undefined;
    return resolveAssetSource_1.default(path) || path;
}
// MARK: - General API
function setupPlayer(options) {
    if (options === void 0) { options = {}; }
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.setupPlayer(options || {})];
        });
    });
}
exports.setupPlayer = setupPlayer;
function destroy() {
    return TrackPlayer.destroy();
}
exports.destroy = destroy;
function updateOptions(options) {
    if (options === void 0) { options = {}; }
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            options = __assign({}, options);
            // Resolve the asset for each icon
            options.icon = resolveImportedPath(options.icon);
            options.playIcon = resolveImportedPath(options.playIcon);
            options.pauseIcon = resolveImportedPath(options.pauseIcon);
            options.stopIcon = resolveImportedPath(options.stopIcon);
            options.previousIcon = resolveImportedPath(options.previousIcon);
            options.nextIcon = resolveImportedPath(options.nextIcon);
            options.rewindIcon = resolveImportedPath(options.rewindIcon);
            options.forwardIcon = resolveImportedPath(options.forwardIcon);
            return [2 /*return*/, TrackPlayer.updateOptions(options)];
        });
    });
}
exports.updateOptions = updateOptions;
function registerPlaybackService(factory) {
    if (react_native_1.Platform.OS === 'android') {
        // Registers the headless task
        react_native_1.AppRegistry.registerHeadlessTask('TrackPlayer', factory);
    }
    else {
        // Initializes and runs the service in the next tick
        setImmediate(factory());
    }
}
exports.registerPlaybackService = registerPlaybackService;
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function addEventListener(event, listener) {
    return emitter.addListener(event, listener);
}
exports.addEventListener = addEventListener;
// MARK: - Queue API
function add(tracks, insertBeforeId) {
    return __awaiter(this, void 0, void 0, function () {
        var i;
        return __generator(this, function (_a) {
            if (!Array.isArray(tracks)) {
                tracks = [tracks];
            }
            if (tracks.length < 1)
                return [2 /*return*/];
            for (i = 0; i < tracks.length; i++) {
                // Clone the object before modifying it
                tracks[i] = __assign({}, tracks[i]);
                // Resolve the URLs
                tracks[i].url = resolveImportedPath(tracks[i].url);
                tracks[i].artwork = resolveImportedPath(tracks[i].artwork);
                // Cast ID's into strings
                tracks[i].id = "" + tracks[i].id;
            }
            return [2 /*return*/, TrackPlayer.add(tracks, insertBeforeId)];
        });
    });
}
exports.add = add;
function remove(tracks) {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            if (!Array.isArray(tracks)) {
                tracks = [tracks];
            }
            return [2 /*return*/, TrackPlayer.remove(tracks)];
        });
    });
}
exports.remove = remove;
function removeUpcomingTracks() {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.removeUpcomingTracks()];
        });
    });
}
exports.removeUpcomingTracks = removeUpcomingTracks;
function skip(trackId) {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.skip(trackId)];
        });
    });
}
exports.skip = skip;
function skipToNext() {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.skipToNext()];
        });
    });
}
exports.skipToNext = skipToNext;
function skipToPrevious() {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.skipToPrevious()];
        });
    });
}
exports.skipToPrevious = skipToPrevious;
function updateMetadataForTrack(trackId, metadata) {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.updateMetadataForTrack(trackId, metadata)];
        });
    });
}
exports.updateMetadataForTrack = updateMetadataForTrack;
// MARK: Playback API
function reset() {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.reset()];
        });
    });
}
exports.reset = reset;
function play() {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.play()];
        });
    });
}
exports.play = play;
function pause() {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.pause()];
        });
    });
}
exports.pause = pause;
function stop() {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.stop()];
        });
    });
}
exports.stop = stop;
function seekTo(position) {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.seekTo(position)];
        });
    });
}
exports.seekTo = seekTo;
function setVolume(level) {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.setVolume(level)];
        });
    });
}
exports.setVolume = setVolume;
function setRate(rate) {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.setRate(rate)];
        });
    });
}
exports.setRate = setRate;
// MARK: - Getters
function getVolume() {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.getVolume()];
        });
    });
}
exports.getVolume = getVolume;
function getRate() {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.getRate()];
        });
    });
}
exports.getRate = getRate;
function getTrack(trackId) {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.getRate(trackId)];
        });
    });
}
exports.getTrack = getTrack;
function getQueue() {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.getQueue()];
        });
    });
}
exports.getQueue = getQueue;
function getCurrentTrack() {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.getCurrentTrack()];
        });
    });
}
exports.getCurrentTrack = getCurrentTrack;
function getDuration() {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.getDuration()];
        });
    });
}
exports.getDuration = getDuration;
function getBufferedPosition() {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.getBufferedPosition()];
        });
    });
}
exports.getBufferedPosition = getBufferedPosition;
function getPosition() {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.getPosition()];
        });
    });
}
exports.getPosition = getPosition;
function getState() {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            return [2 /*return*/, TrackPlayer.getState()];
        });
    });
}
exports.getState = getState;
// MARK: - State Constants
exports.STATE_NONE = TrackPlayer.STATE_NONE;
exports.STATE_READY = TrackPlayer.STATE_READY;
exports.STATE_PLAYING = TrackPlayer.STATE_PLAYING;
exports.STATE_PAUSED = TrackPlayer.STATE_PAUSED;
exports.STATE_STOPPED = TrackPlayer.STATE_STOPPED;
exports.STATE_BUFFERING = TrackPlayer.STATE_BUFFERING;
exports.STATE_CONNECTING = TrackPlayer.STATE_CONNECTING;
// MARK: - Capabilities Constants
exports.CAPABILITY_PLAY = TrackPlayer.CAPABILITY_PLAY;
exports.CAPABILITY_PLAY_FROM_ID = TrackPlayer.CAPABILITY_PLAY_FROM_ID;
exports.CAPABILITY_PLAY_FROM_SEARCH = TrackPlayer.CAPABILITY_PLAY_FROM_SEARCH;
exports.CAPABILITY_PAUSE = TrackPlayer.CAPABILITY_PAUSE;
exports.CAPABILITY_STOP = TrackPlayer.CAPABILITY_STOP;
exports.CAPABILITY_SEEK_TO = TrackPlayer.CAPABILITY_SEEK_TO;
exports.CAPABILITY_SKIP = TrackPlayer.CAPABILITY_SKIP;
exports.CAPABILITY_SKIP_TO_NEXT = TrackPlayer.CAPABILITY_SKIP_TO_NEXT;
exports.CAPABILITY_SKIP_TO_PREVIOUS = TrackPlayer.CAPABILITY_SKIP_TO_PREVIOUS;
exports.CAPABILITY_JUMP_FORWARD = TrackPlayer.CAPABILITY_JUMP_FORWARD;
exports.CAPABILITY_JUMP_BACKWARD = TrackPlayer.CAPABILITY_JUMP_BACKWARD;
exports.CAPABILITY_SET_RATING = TrackPlayer.CAPABILITY_SET_RATING;
exports.CAPABILITY_LIKE = TrackPlayer.CAPABILITY_LIKE;
exports.CAPABILITY_DISLIKE = TrackPlayer.CAPABILITY_DISLIKE;
exports.CAPABILITY_BOOKMARK = TrackPlayer.CAPABILITY_BOOKMARK;
// MARK: - Pitch Constants
exports.PITCH_ALGORITHM_LINEAR = TrackPlayer.PITCH_ALGORITHM_LINEAR;
exports.PITCH_ALGORITHM_MUSIC = TrackPlayer.PITCH_ALGORITHM_MUSIC;
exports.PITCH_ALGORITHM_VOICE = TrackPlayer.PITCH_ALGORITHM_VOICE;
// MARK: - Rating Constants
exports.RATING_HEART = TrackPlayer.RATING_HEART;
exports.RATING_THUMBS_UP_DOWN = TrackPlayer.RATING_THUMBS_UP_DOWN;
exports.RATING_3_STARS = TrackPlayer.RATING_3_STARS;
exports.RATING_4_STARS = TrackPlayer.RATING_4_STARS;
exports.RATING_5_STARS = TrackPlayer.RATING_5_STARS;
exports.RATING_PERCENTAGE = TrackPlayer.RATING_PERCENTAGE;
