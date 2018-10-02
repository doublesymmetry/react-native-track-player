using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Diagnostics;
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

        private IPromise loadCallback;

        private bool started = false;
        private bool ended = false;
        private double startPos = 0;

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

        public override SystemMediaTransportControls GetTransportControls() {
            return player.SystemMediaTransportControls;
        }

        protected override void Load(Track track, IPromise promise) {
            started = false;
            ended = false;
            startPos = 0;
            loadCallback = promise;

            player.Source = MediaSource.CreateFromUri(track.url);
            // TODO: check whether adaptive streaming works without "CreateFromAdaptiveMediaSource"
        }

        public override void Play() {
            started = true;
            ended = false;
            player.Play();
        }

        public override void Pause() {
            started = false;
            player.Pause();
        }

        public override void Stop() {
            started = false;
            ended = true;
            player.Pause();
            player.PlaybackSession.Position = TimeSpan.FromSeconds(0);
        }

        public override void SetVolume(double volume) {
            player.Volume = volume;
        }

        public override double GetVolume() {
            return player.Volume;
        }

        public override void SetRate(double rate) {
            player.PlaybackSession.PlaybackRate = rate;
        }

        public override double GetRate() {
            return player.PlaybackSession.PlaybackRate;
        }

        public override void SeekTo(double seconds) {
            startPos = seconds;
            player.PlaybackSession.Position = TimeSpan.FromSeconds(seconds);
        }

        public override double GetPosition() {
            return player.PlaybackSession.Position.TotalSeconds;
        }

        public override double GetBufferedPosition() {
#pragma warning disable CS0168 // Unused exception variable
            try {
                return player.PlaybackSession.BufferingProgress * GetDuration();
            } catch(Exception ex) {
                return 0;
            }
#pragma warning restore CS0168
        }

        public override double GetDuration() {
            double duration = player.PlaybackSession.NaturalDuration.TotalSeconds;

            if(duration <= 0) {
                Track track = GetCurrentTrack();
                duration = track != null && track.duration > 0 ? track.duration : 0;
            }

            return duration;
        }

        public override MediaPlaybackState GetState() {
            MediaPlaybackState state = player.PlaybackSession.PlaybackState;

            if(ended && Utils.IsPaused(state)) {
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
                Play();
            } else {
                manager.OnEnd(GetCurrentTrack(), GetPosition());
            }
        }

        private void OnError(MediaPlayer sender, MediaPlayerFailedEventArgs args) {
            loadCallback?.Reject("load", args.ErrorMessage);
            loadCallback = null;

            Debug.WriteLine(args.Error);
            Debug.WriteLine(args.ErrorMessage);
            Debug.WriteLine(args.ExtendedErrorCode);

            manager.OnError(args.ErrorMessage);
        }

        private void OnLoad(MediaPlayer sender, object args) {
            Debug.WriteLine("OnLoad");

            if(startPos > 0) {
                player.PlaybackSession.Position = TimeSpan.FromSeconds(startPos);
                startPos = 0;
            }

            if(started) Play();

            loadCallback?.Resolve(null);
            loadCallback = null;
        }
    }
}
