using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using ReactNative.Bridge;
using System.Text;
using System.Threading.Tasks;
using Windows.Media;
using Windows.Media.Playback;
using Windows.Media.Core;
using TrackPlayer.Logic;

namespace TrackPlayer.Players {

    class LocalPlayback : Playback {

        private MediaPlayer player;

        private bool stopped = false;

        private IPromise loadCallback;

        public LocalPlayback(MediaManager manager, JObject options) : base(manager) {
            player = new MediaPlayer();
            player.AutoPlay = false;
            player.AudioCategory = MediaPlayerAudioCategory.Media;
            player.CommandManager.IsEnabled = false;

            player.MediaOpened += OnLoad;
            player.MediaFailed += OnError;
            player.MediaEnded += OnEnd;
            player.CurrentStateChanged += OnStateChange;
        }

        protected override void Load(Track track, IPromise promise) {
            stopped = false;
            loadCallback = promise;
            player.Source = MediaSource.CreateFromUri(track.url);
            // TODO: check whether adaptive streaming works without "CreateFromAdaptiveMediaSource"
        }

        public override void Play() {
            stopped = false;
            player.Play();
        }

        public override void Pause() {
            stopped = false;
            player.Pause();
        }

        public override void Stop() {
            stopped = true;
            player.Pause();
            player.PlaybackSession.Position = TimeSpan.FromSeconds(0);
        }

        public override void SetVolume(double volume) {
            player.Volume = volume;
        }

        public override double GetVolume() {
            return player.Volume;
        }

        public override void SeekTo(double seconds) {
            player.PlaybackSession.Position = TimeSpan.FromSeconds(seconds);
        }

        public override double GetPosition() {
            return player.PlaybackSession.Position.TotalSeconds;
        }

        public override double GetBufferedPosition() {
            return player.PlaybackSession.BufferingProgress * GetDuration();
        }

        public override double GetDuration() {
            double duration = player.PlaybackSession.NaturalDuration.TotalSeconds;

            return duration <= 0 ? GetCurrentTrack().duration : duration;
        }

        public override MediaPlaybackState GetState() {
            MediaPlaybackState state = player.PlaybackSession.PlaybackState;

            if(stopped && Utils.IsPaused(state)) {
                state = MediaPlaybackState.None;
            }

            return Utils.GetState(state);
        }

        public override void Dispose() {
            player.Dispose();
        }

        private void OnStateChange(MediaPlayer sender, object args) {
            UpdateState(GetState());
        }

        private void OnEnd(MediaPlayer sender, object args) {
            if(HasNext()) {
                UpdateCurrentTrack(currentTrack + 1, null);
            } else {
                manager.OnEnd();
            }
        }

        private void OnError(MediaPlayer sender, MediaPlayerFailedEventArgs args) {
            loadCallback?.Reject("load", args.ErrorMessage);
            loadCallback = null;

            manager.OnError(args.ErrorMessage);
        }

        private void OnLoad(MediaPlayer sender, object args) {
            loadCallback?.Resolve(null);
            loadCallback = null;
        }
    }
}
