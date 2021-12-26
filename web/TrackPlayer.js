import { DeviceEventEmitter } from "react-native";
import { RepeatMode, State, Event, Capability } from "react-native-track-player";
import MediaSession from "./MediaSession";

export class TrackPlayerModule {
    static STATE_NONE = 0;
    static STATE_READY = 1;
    static STATE_PLAYING = 2;
    static STATE_PAUSED = 3;
    static STATE_STOPPED = 4;
    static STATE_BUFFERING = 5;
    static STATE_CONNECTING = 6;

    static PLAYBACK_STATE = "playback-state";
    static PLAYBACK_TRACK_CHANGED = "playback-track-changed";
    static PLAYBACK_QUEUE_ENDED = "playback-queue-ended";
    static PLAYBACK_ERROR = "playback-error";
    static PLAYBACK_METADATA_RECEIVED = "playback-metadata-received";
    
    static REMOTE_PLAY = "remote-play";
    static REMOTE_PAUSE = "remote-pause";
    static REMOTE_STOP = "remote-stop";
    static REMOTE_JUMP_BACKWARD = "remote-jump-backward"
    static REMOTE_JUMP_FORWARD = "remote-jump-forward"
    static REMOTE_SEEK = "remote-seek";
    static REMOTE_NEXT = "remote-next";
    static REMOTE_PREVIOUS = "remote-previous";

    static REPEAT_OFF = "Off";
    static REPEAT_TRACK = "Track";
    static REPEAT_QUEUE = "Queue";

    static CAPABILITY_PLAY = 0;
    static CAPABILITY_PLAY_FROM_ID = 1;
    static CAPABILITY_PLAY_FROM_SEARCH = 2;
    static CAPABILITY_PAUSE = 3;
    static CAPABILITY_STOP = 4;
    static CAPABILITY_SEEK_TO = 5;
    static CAPABILITY_SKIP = 6;
    static CAPABILITY_SKIP_TO_NEXT = 7;
    static CAPABILITY_SKIP_TO_PREVIOUS = 8;
    static CAPABILITY_JUMP_FORWARD = 9;
    static CAPABILITY_JUMP_BACKWARD = 10;
    static CAPABILITY_SET_RATING = 11;
    static CAPABILITY_LIKE = 12;
    static CAPABILITY_DISLIKE = 13;
    static CAPABILITY_BOOKMARK = 14;

    static RATING_HEART = 0;
    static RATING_THUMBS_UP_DOWN = 1;
    static RATING_3_STARS = 2;
    static RATING_4_STARS = 3;
    static RATING_5_STARS = 4;
    static RATING_PERCENTAGE = 5;

    static #emitter;

    static #currentIndex;
    static #playlist;
    static #track;
    static #index;
    static #playEnded;
    static #audio;
    static #repeatMode;

    static #emitNextTrack = index => {
        let position = this.#audio.src != ''
            ? this.#audio.currentTime
            : -0.01

        this.#emitter.emit(
            Event.PlaybackTrackChanged,
            {nextTrack: index, position: position,track: this.#currentIndex}
        );

        this.#currentIndex = index;
    }

    static play = () => {
        if (this.#audio.src != '') {
            this.#audio.play()
                .then(() => {
                    if (!MediaSession.actionsEnabled)
                        MediaSession.enableCapabilities();
                })
                .catch(() => {
                    this.#emitter.emit(
                        Event.PlaybackState, { state: State.Paused}
                    );
                });;
        }
    }

    static pause = () => {
        if (this.#audio.src != '') {
            this.#audio.pause();
        }
    }

    static remove = index => {
        return new Promise((resolve, reject) => {
            if (index < 0 || index > this.#playlist.length - 1)
                resolve();
            
            if (typeof index == "object")
                this.#playlist = this.#playlist.filter(
                    (val, i) => !index.includes(i)
                );
            else
                this.#playlist = this.#playlist.filter(
                    (val, i) => i != index
                );

            resolve();
        });
    }

    static add = (tracks, insertBeforeIndex) => {
        return new Promise((resolve, reject) => {
            if (this.#playlist == null)
                this.#playlist = [];

            if (insertBeforeIndex == -1)
                insertBeforeIndex = this.#playlist.length;
            
            this.#playlist = this.#playlist.slice(0, insertBeforeIndex)
                .concat(
                    tracks,
                    this.#playlist.slice(insertBeforeIndex)
                );

            resolve();
        });
    }

    static stop = () => {
        return new Promise((resolve, reject) => {
            if (this.#audio.src != '') {
                this.#audio.pause();
                this.#emitter.emit(Event.PlaybackState, {state: State.Stopped});
            }
            resolve();
        });
    }

    static reset = () => {
        return new Promise((resolve, reject) => {
            if (this.#audio.src != '')
                this.#audio.pause();

            this.#track = null;
            this.#currentIndex = null;
            this.#playlist = [];
            this.#index = 0;
            this.#emitter.emit(Event.PlaybackState, {state: State.None});
            resolve();
        });
    }

    static destroy = () => {
        return this.reset();
    }

    static skip = index => {
        return new Promise((resolve, reject) => {
            if (index < 0 || index >= this.#playlist.length)
                resolve();

            this.#index = index;
            this.#track = this.#playlist[index];
            this.#audio.src = this.#track.url;
            this.#emitNextTrack(index);
            MediaSession.setMetadata(
                this.#track.title,
                this.#track.artist,
                this.#track.artwork
            );

            this.getPosition().then(position => {
                MediaSession.setPosition(
                    this.#track.duration,
                    position
                );
                
                resolve();
            });
        });
    }

    static skipToNext = async(wasPlaying) => {
        if (this.#playlist != null) {
            let nextIndex;
            if ((this.#index + 1) == this.#playlist.length) {
                if (this.#repeatMode == RepeatMode.Off) {
                    this.seekTo(0);
                    return;
                } else if (this.#repeatMode == RepeatMode.Queue) {
                    nextIndex = 0;
                }
            } else {
                nextIndex = this.#index + 1;
            }

            this.skip(nextIndex);
            if (!wasPlaying)
                this.pause();
        }
    }

    static skipToPrevious = () => {
        if (this.#playlist != null) {
            if (this.#index == 0) {
                this.seekTo(0);
                return;
            }

            this.skip(this.#index - 1);

            if (!wasPlaying)
                this.pause();
        }
    }

    static removeUpcomingTracks = () => {
        return new Promise((resolve, reject) => {
            if (this.#playlist != null) {
                if (this.#playlist.length > 0) {
                    this.#playlist = this.#playlist.slice(0, this.#index)
                }
            }
            resolve();
        });
    }

    static setVolume = volume => {
        this.#audio.volume = volume;
    }

    static setRate = rate => {
        this.#audio.playbackRate = rate;
    }

    static setRepeatMode = mode => {
        if (Object.values(RepeatMode).includes(mode))
            this.#repeatMode = mode;
        
        if (mode == RepeatMode.Track)
            this.#audio.loop = true;
        else
            this.#audio.loop = false;
    }

    static getRepeatMode = () => {
        return this.#repeatMode;
    }

    static seekTo = seconds => {
        return new Promise((resolve, reject) => {
            if (this.#audio.src != '') {
                if (this.#audio.fastSeek != undefined)
                    this.#audio.fastSeek(seconds);
                else
                    this.#audio.currentTime = seconds;

                MediaSession.setPosition(
                    this.#track.duration,
                    seconds
                );
            }
            resolve(seconds);
        });
    }

    static getTrack = index => {
        return new Promise((resolve, reject) => {
            if (index < 0 || index > this.#playlist.length - 1)
                resolve(null);
            else
                resolve(this.#playlist[index]);
        });
    }

    static getCurrentTrack = () => {
        return new Promise((resolve, reject) => {
            if (this.#track != null)
                resolve(this.#index);
            else
                resolve(null);
        });
    }

    static getPosition = () => {
        return new Promise((resolve, reject) => {
            if (this.#audio.src || this.#audio.readyState == this.#audio.HAVE_ENOUGH_DATA)
                resolve(this.#audio.currentTime);
            else
                resolve(0);
        });
    }

    static getVolume = () => {
        return this.#audio.volume;
    }

    static getDuration = () => {
        return new Promise((resolve, reject) => {
            if (this.#audio.src != '' && this.#track != null)
                resolve(this.#track.duration);
            else
                resolve(0);
        });
    }

    static getBufferedPosition = () => {
        return new Promise((resolve, reject) => {
            if (this.#audio.src != '')
                resolve(this.#audio.buffered);
        });
    }

    static getState = () => {
        return new Promise((resolve, reject) => {
            if (this.#audio.src == '')
                resolve(State.None);
            else {
                if (this.#audio.paused)
                    resolve(State.Paused);
                else
                    resolve(State.Playing);
            }
        });
    }

    static getRate = () => {
        return new Promise((resolve, reject) => {
            if (this.#audio.src != '')
                resolve(this.#audio.defaultPlaybackRate);
            else
                resolve(null);
        });
    }

    static getQueue = () => {
        return new Promise((resolve, reject) => {
            resolve(this.#playlist);
        });
    }

    static setupPlayer = () => {
        return new Promise((resolve, reject) => {
            this.#emitter = DeviceEventEmitter;
            this.#playlist = [];
            this.#currentIndex = null;
            this.#track = null;
            this.#index = null;
            this.#playEnded = false;
            
            this.#audio = document.createElement("audio");
            this.#audio.onended = e => {
                if (this.#repeatMode == RepeatMode.Off) {
                    if (this.#playlist.length - 1 == this.#index) {
                        MediaSession.setPaused();
                        this.#playEnded = true;
                        this.#audio.src = this.#track.url;
                        this.#emitter.emit(Event.PlaybackState, { state: State.Paused});
                        this.#emitter.emit(
                            Event.PlaybackQueueEnded,
                            {
                                track: this.#currentIndex,
                                position: this.#audio.currentTime
                            }
                        );
                    } else {
                        this.skipToNext(true);
                    }

                } else if (this.#repeatMode == RepeatMode.Queue) {
                    if (this.#currentIndex < this.#playlist.length)
                        this.skipToNext(true);
                    else
                        this.skip(0);
                } else {
                    this.seekTo(0).then(() => {
                        this.#emitter.emit(Event.PlaybackState, { state: State.Playing});
                    });
                }
            };

            this.#audio.oncanplay = e => {
                this.play();

                if (this.#playEnded) {
                    this.#playEnded = false;
                    this.#audio.pause();
                }
            };

            this.#audio.onpause = e => {
                if (this.#track != null) {
                    this.#emitter.emit(Event.PlaybackState, {state: State.Paused});
                    MediaSession.setPaused();
                }
            };

            this.#audio.onplay = e => {
                if (this.#track != null) {
                    this.#emitter.emit(Event.PlaybackState, {state: State.Playing});
                    MediaSession.setPlaying();
                }
            };
            
            document.body.appendChild(this.#audio);
            resolve();
        });
    };

    static updateOptions = options => {
        return new Promise((resolve, reject) => {
            let actionHandlers = [];
            if (options.capabilities.includes(Capability.Play))
                actionHandlers.push(['play', () => this.#emitter.emit(Event.RemotePlay)]);
            
            if (options.capabilities.includes(Capability.Pause))
                actionHandlers.push(['pause', () => this.#emitter.emit(Event.RemotePause)]);
            
            if (options.capabilities.includes(Capability.Stop))
                actionHandlers.push(['stop', () => this.#emitter.emit(Event.RemoteStop)]);

            if (options.capabilities.includes(Capability.SeekTo)) {
                actionHandlers.push(['seekbackward', () => this.#emitter.emit(Event.RemoteJumpBackward)]);
                actionHandlers.push(['seekforward', () => this.#emitter.emit(Event.RemoteJumpForward)]);
                actionHandlers.push(['seekto', () => this.#emitter.emit(Event.RemoteSeek)]);
            }

            if (options.capabilities.includes(Capability.SkipToPrevious))
                actionHandlers.push(['previoustrack', () => this.#emitter.emit(Event.RemotePrevious)]);
            
            if (options.capabilities.includes(Capability.SkipToNext))
                actionHandlers.push(['nexttrack', () => this.#emitter.emit(Event.RemoteNext)]);

            MediaSession.setCapabilities(actionHandlers);
            resolve();
        });
    }
}