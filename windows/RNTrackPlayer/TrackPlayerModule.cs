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

namespace TrackPlayer {

    class TrackPlayerModule : ReactContextNativeModuleBase, ILifecycleEventListener {

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
                    {"STATE_NONE", MediaPlayerState.Closed},
                    {"STATE_PLAYING", MediaPlayerState.Playing},
                    {"STATE_PAUSED", MediaPlayerState.Paused},
                    {"STATE_STOPPED", MediaPlayerState.Stopped},
                    {"STATE_BUFFERING", MediaPlayerState.Buffering},

                    {"CAPABILITY_PLAY", Capability.Play},
                    {"CAPABILITY_PLAY_FROM_ID", Capability.Unsupported},
                    {"CAPABILITY_PLAY_FROM_SEARCH", Capability.Unsupported},
                    {"CAPABILITY_PAUSE", Capability.Pause},
                    {"CAPABILITY_STOP", Capability.Stop},
                    {"CAPABILITY_SEEK_TO", Capability.Seek},
                    {"CAPABILITY_SKIP", Capability.Unsupported},
                    {"CAPABILITY_SKIP_TO_NEXT", Capability.Next},
                    {"CAPABILITY_SKIP_TO_PREVIOUS", Capability.Previous},
                    {"CAPABILITY_SET_RATING", Capability.Unsupported},

                    // Rating is unsupported
                    {"RATING_HEART", 0},
                    {"RATING_THUMBS_UP_DOWN", 0},
                    {"RATING_3_STARS", 0},
                    {"RATING_4_STARS", 0},
                    {"RATING_5_STARS", 0},
                    {"RATING_PERCENTAGE", 0},

                    // Cast is unsupported (for now)
                    {"CAST_NO_DEVICES_AVAILABLE", 0},
                    {"CAST_NOT_CONNECTED", 0},
                    {"CAST_CONNECTING", 0},
                    {"CAST_CONNECTED", 0},
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
        public async void setupPlayer(JObject options, IPromise promise) {
            if(manager != null) {
                promise.Resolve(null);
                return;
            }

            manager = new MediaManager(options);
            promise.Resolve(null);
        }

        [ReactMethod]
        public async void destroy() {
            if(manager != null) {
                manager.Dispose();
                manager = null;
            }
        }

        [ReactMethod]
        public async void updateOptions(JObject options) {
            manager.UpdateOptions(options);
        }

        [ReactMethod]
        public async void play() {
            manager.GetPlayer().Play();
        }

        [ReactMethod]
        public async void pause() {
            manager.GetPlayer().Pause();
        }

        [ReactMethod]
        public async void stop() {
            manager.GetPlayer().Stop();
        }

        [ReactMethod]
        public async void reset() {
            manager.GetPlayer().Reset();
        }

        [ReactMethod]
        public async void add(JArray array, string insertBeforeId, IPromise promise) {
            List<Track> tracks = new List<Track>(array.Count);

            foreach(JObject obj in array) {
                tracks.Add(new Track(obj));
            }

            manager.GetPlayer().Add(tracks, insertBeforeId, promise);
        }

        [ReactMethod]
        public async void remove(JArray array, IPromise promise) {
            List<string> tracks = new List<string>(array.Count);

            foreach(string id in array) {
                tracks.Add(id);
            }

            manager.GetPlayer().Remove(tracks, promise);
        }

        [ReactMethod]
        public async void skip(string track, IPromise promise) {
            manager.GetPlayer().Skip(track, promise);
        }

        [ReactMethod]
        public async void skipToNext(IPromise promise) {
            manager.GetPlayer().SkipToNext(promise);
        }

        [ReactMethod]
        public async void skipToPrevious(IPromise promise) {
            manager.GetPlayer().SkipToPrevious(promise);
        }

        [ReactMethod]
        public async string getCurrentTrack() {
            return manager.GetPlayer().GetCurrentTrack()?.id;
        }

        [ReactMethod]
        public async JObject getTrack(string id) {
            return manager.GetPlayer().GetTrack(id)?.ToObject();
        }

        [ReactMethod]
        public async double getVolume() {
            return manager.GetPlayer().GetVolume();
        }

        [ReactMethod]
        public async void setVolume(double volume) {
            manager.GetPlayer().SetVolume(volume);
        }

        [ReactMethod]
        public async void seekTo(double seconds) {
            manager.GetPlayer().SeekTo(seconds);
        }

        [ReactMethod]
        public async double getPosition() {
            return manager.GetPlayer().GetPosition();
        }

        [ReactMethod]
        public async double getBufferedPosition() {
            return manager.GetPlayer().GetBufferedPosition();
        }

        [ReactMethod]
        public async double getDuration() {
            return manager.GetPlayer().GetDuration();
        }

        [ReactMethod]
        public async int getState() {
            return manager.GetPlayer().GetState();
        }

        [ReactMethod]
        public async int getCastState() {
            // TODO
            return 0;
        }

    }
}
