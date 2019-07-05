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
var _this = this;
Object.defineProperty(exports, "__esModule", { value: true });
var react_1 = require("react");
var index_1 = require("./index");
/**
 * @description
 *   Get current playback state and subsequent updatates
 */
exports.usePlaybackState = function () {
    var _a = react_1.useState(index_1.STATE_NONE), state = _a[0], setState = _a[1];
    react_1.useEffect(function () {
        function setPlayerState() {
            return __awaiter(this, void 0, void 0, function () {
                var playerState;
                return __generator(this, function (_a) {
                    switch (_a.label) {
                        case 0: return [4 /*yield*/, index_1.getState()];
                        case 1:
                            playerState = _a.sent();
                            setState(playerState);
                            return [2 /*return*/];
                    }
                });
            });
        }
        setPlayerState();
        var sub = index_1.addEventListener(index_1.Event.PlaybackState, function (data) {
            setState(data.state);
        });
        return function () {
            sub.remove();
        };
    }, []);
    return state;
};
/**
 * Attaches a handler to the given TrackPlayer events and performs cleanup on unmount
 * @param events - TrackPlayer events to subscribe to
 * @param handler - callback invoked when the event fires
 */
exports.useTrackPlayerEvents = function (events, handler) {
    var savedHandler = react_1.useRef();
    react_1.useEffect(function () {
        savedHandler.current = handler;
    }, [handler]);
    react_1.useEffect(function () {
        if (__DEV__) {
            var allowedTypes_1 = Object.values(index_1.Event);
            var invalidTypes = events.filter(function (type) { return !allowedTypes_1.includes(type); });
            if (invalidTypes.length) {
                console.warn('One or more of the events provided to useTrackPlayerEvents is ' +
                    ("not a valid TrackPlayer event: " + invalidTypes.join("', '") + ". ") +
                    'A list of available events can be found at ' +
                    'https://react-native-kit.github.io/react-native-track-player/documentation/#events');
            }
        }
        var subs = events.map(function (event) {
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            return index_1.addEventListener(event, function (payload) { return savedHandler.current(__assign({}, payload, { type: event })); });
        });
        return function () {
            subs.forEach(function (sub) { return sub.remove(); });
        };
    }, events);
};
var useInterval = function (callback, delay) {
    var savedCallback = react_1.useRef();
    react_1.useEffect(function () {
        savedCallback.current = callback;
    });
    react_1.useEffect(function () {
        if (!delay)
            return;
        var id = setInterval(savedCallback.current, delay);
        return function () { return clearInterval(id); };
    }, [delay]);
};
var useWhenPlaybackStateChanges = function (callback) {
    exports.useTrackPlayerEvents([index_1.Event.PlaybackState], function (_a) {
        var state = _a.state;
        callback(state);
    });
    react_1.useEffect(function () {
        var didCancel = false;
        var fetchPlaybackState = function () { return __awaiter(_this, void 0, void 0, function () {
            var playbackState;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, index_1.getState()];
                    case 1:
                        playbackState = _a.sent();
                        if (!didCancel) {
                            callback(playbackState);
                        }
                        return [2 /*return*/];
                }
            });
        }); };
        fetchPlaybackState();
        return function () {
            didCancel = true;
        };
    }, []);
};
var usePlaybackStateIs = function () {
    var states = [];
    for (var _i = 0; _i < arguments.length; _i++) {
        states[_i] = arguments[_i];
    }
    var _a = react_1.useState(), is = _a[0], setIs = _a[1];
    useWhenPlaybackStateChanges(function (state) {
        setIs(states.includes(state));
    });
    return is;
};
/**
 * Poll for track progress for the given interval (in miliseconds)
 * @param interval - ms interval
 */
exports.useTrackPlayerProgress = function (interval) {
    if (interval === void 0) { interval = 1000; }
    var initialState = {
        position: 0,
        bufferedPosition: 0,
        duration: 0,
    };
    var _a = react_1.useState(initialState), state = _a[0], setState = _a[1];
    var needsPoll = usePlaybackStateIs(index_1.STATE_PLAYING, index_1.STATE_BUFFERING);
    var getProgress = function () { return __awaiter(_this, void 0, void 0, function () {
        var _a, position, bufferedPosition, duration;
        return __generator(this, function (_b) {
            switch (_b.label) {
                case 0:
                    if (!needsPoll)
                        return [2 /*return*/];
                    return [4 /*yield*/, Promise.all([
                            index_1.getPosition(),
                            index_1.getBufferedPosition(),
                            index_1.getDuration(),
                        ])];
                case 1:
                    _a = _b.sent(), position = _a[0], bufferedPosition = _a[1], duration = _a[2];
                    setState({ position: position, bufferedPosition: bufferedPosition, duration: duration });
                    return [2 /*return*/];
            }
        });
    }); };
    useInterval(getProgress, interval);
    return state;
};
