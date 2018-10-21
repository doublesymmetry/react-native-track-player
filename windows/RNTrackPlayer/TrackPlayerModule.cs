using Newtonsoft.Json.Linq;
using ReactNative.Bridge;
using ReactNative.Collections;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;
using Windows.UI.Popups;
using Windows.Media.Playback;
using TrackPlayer.Logic;
using TrackPlayer.Players;

namespace TrackPlayer
{ // <- Put the bracket in a new line here to prevent linking errors

    class TrackPlayerModule : ReactContextNativeModuleBase {

        private MediaManager manager;

        public TrackPlayerModule(ReactContext reactContext) : base(reactContext) {

        }

        public override string Name {
            get {
                return "TrackPlayerModule";
            }
        }

        public override IReadOnlyDictionary<string, object> Constants {
            get {
                return new Dictionary<string, object> {
                    {"STATE_NONE", (int)MediaPlaybackState.None},
                    {"STATE_PLAYING", (int)MediaPlaybackState.Playing},
                    {"STATE_PAUSED", (int)MediaPlaybackState.Paused},
                    {"STATE_STOPPED", -1}, // Unsupported
                    {"STATE_BUFFERING", (int)MediaPlaybackState.Buffering},

                    {"CAPABILITY_PLAY", (int)Capability.Play},
                    {"CAPABILITY_PLAY_FROM_ID", (int)Capability.Unsupported},
                    {"CAPABILITY_PLAY_FROM_SEARCH", (int)Capability.Unsupported},
                    {"CAPABILITY_PAUSE", (int)Capability.Pause},
                    {"CAPABILITY_STOP", (int)Capability.Stop},
                    {"CAPABILITY_SEEK_TO", (int)Capability.Seek},
                    {"CAPABILITY_SKIP", (int)Capability.Unsupported},
                    {"CAPABILITY_SKIP_TO_NEXT", (int)Capability.Next},
                    {"CAPABILITY_SKIP_TO_PREVIOUS", (int)Capability.Previous},
                    {"CAPABILITY_SET_RATING", (int)Capability.Unsupported},

                    // Pitch algorithms is unsupported
                    {"PITCH_ALGORITHM_LINEAR", 0},
                    {"PITCH_ALGORITHM_MUSIC", 0},
                    {"PITCH_ALGORITHM_VOICE", 0},

                    // Rating is unsupported
                    {"RATING_HEART", 0},
                    {"RATING_THUMBS_UP_DOWN", 0},
                    {"RATING_3_STARS", 0},
                    {"RATING_4_STARS", 0},
                    {"RATING_5_STARS", 0},
                    {"RATING_PERCENTAGE", 0},

                    // Cast is unsupported (for now)
                    {"CAST_NO_DEVICES_AVAILABLE", 0},
                    {"CAST_NOT_CONNECTED", 1},
                    {"CAST_CONNECTING", 2},
                    {"CAST_CONNECTED", 3},
                    {"CAST_SUPPORT_AVAILABLE", false}
                };
            }
        }

        public override void OnReactInstanceDispose() {
            base.OnReactInstanceDispose();

            if(manager != null) {
                manager.Dispose();
                manager = null;
            }
        }

        [ReactMethod]
        public void setupPlayer(JObject options, IPromise promise) {
            if(manager != null) {
                promise.Resolve(null);
                return;
            }

            manager = new MediaManager(Context, options);
            promise.Resolve(null);
        }

        [ReactMethod]
        public void destroy() {
            if(manager != null) {
                manager.Dispose();
                manager = null;
            }
        }

        [ReactMethod]
        public void updateOptions(JObject options, IPromise promise) {
            // TODO remove the necessity of setupPlayer
            manager?.UpdateOptions(options);
            promise.Resolve(null);
        }

        [ReactMethod]
        public void play(IPromise promise) {
            manager?.GetPlayer()?.Play();
            promise.Resolve(null);
        }

        [ReactMethod]
        public void pause(IPromise promise) {
            manager?.GetPlayer()?.Pause();
            promise.Resolve(null);
        }

        [ReactMethod]
        public void stop(IPromise promise) {
            manager?.GetPlayer()?.Stop();
            promise.Resolve(null);
        }

        [ReactMethod]
        public void reset(IPromise promise) {
            manager?.GetPlayer()?.Reset();
            promise.Resolve(null);
        }

        [ReactMethod]
        public void add(JArray array, string insertBeforeId, IPromise promise) {
            List<Track> tracks = new List<Track>(array.Count);

            foreach(JObject obj in array) {
                tracks.Add(new Track(obj));
            }

            manager?.GetPlayer()?.Add(tracks, insertBeforeId, promise);
        }

        [ReactMethod]
        public void remove(JArray array, IPromise promise) {
            List<string> tracks = new List<string>(array.Count);

            foreach(string id in array) {
                tracks.Add(id);
            }

            manager?.GetPlayer()?.Remove(tracks, promise);
        }

        [ReactMethod]
        public void skip(string track, IPromise promise) {
            manager?.GetPlayer()?.Skip(track, promise);
        }

        [ReactMethod]
        public void skipToNext(IPromise promise) {
            manager?.GetPlayer()?.SkipToNext(promise);
        }

        [ReactMethod]
        public void skipToPrevious(IPromise promise) {
            manager?.GetPlayer()?.SkipToPrevious(promise);
        }

        [ReactMethod]
        public void getCurrentTrack(IPromise promise) {
            Playback player = manager?.GetPlayer();
            if(Utils.CheckPlayback(player, promise)) return;

            string id = player.GetCurrentTrack()?.id;

            if(id != null) {
                promise.Resolve(id);
            } else {
                promise.Reject("track", "No track playing");
            }
        }

        [ReactMethod]
        public void getTrack(string id, IPromise promise) {
            Playback player = manager?.GetPlayer();
            if(Utils.CheckPlayback(player, promise)) return;

            JObject track = player.GetTrack(id)?.ToObject();

            if(track != null) {
                promise.Resolve(track);
            } else {
                promise.Resolve(null);
            }
        }

        [ReactMethod]
        public void getVolume(IPromise promise) {
            Playback player = manager?.GetPlayer();
            if(Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetVolume());
        }

        [ReactMethod]
        public void setVolume(double volume, IPromise promise) {
            manager?.GetPlayer()?.SetVolume(volume);
            promise.Resolve(null);
        }

        [ReactMethod]
        public void getRate(IPromise promise) {
            Playback player = manager?.GetPlayer();
            if(Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetRate());
        }

        [ReactMethod]
        public void setRate(double rate, IPromise promise) {
            manager?.GetPlayer()?.SetRate(rate);
            promise.Resolve(null);
        }

        [ReactMethod]
        public void seekTo(double seconds, IPromise promise) {
            manager?.GetPlayer()?.SeekTo(seconds);
            promise.Resolve(null);
        }

        [ReactMethod]
        public void getPosition(IPromise promise) {
            Playback player = manager?.GetPlayer();
            if(Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetPosition());
        }

        [ReactMethod]
        public void getBufferedPosition(IPromise promise) {
            Playback player = manager?.GetPlayer();
            if(Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetBufferedPosition());
        }

        [ReactMethod]
        public void getDuration(IPromise promise) {
            Playback player = manager?.GetPlayer();
            if(Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetDuration());
        }

        [ReactMethod]
        public void getState(IPromise promise) {
            Playback player = manager?.GetPlayer();
            if(Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetState());
        }
    }
}
