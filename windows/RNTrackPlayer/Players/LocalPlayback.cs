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

        private IPromise loadCallback;

        public LocalPlayback(JObject options) {
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
            loadCallback = promise;
            player.SetUriSource(track.url);
        }

        public override void Play() {
            player.Play();
        }

        public override void Pause() {
            player.Pause();
        }

        public override void Stop() {
            player.Stop();
        }

        public override void SetVolume(double volume) {
            player.Volume = volume;
        }

        public override double GetVolume() {
            return player.Volume;
        }

        public override void SeekTo(double seconds) {
            player.Position = TimeSpan.FromSeconds(seconds);
        }

        public override double GetPosition() {
            return player.Position.TotalSeconds;
        }

        public override double GetBufferedPosition() {
            return player.BufferingProgress * GetDuration();
        }

        public override double GetDuration() {
            return player.NaturalDuration.TotalSeconds;
        }

        public override MediaPlayerState GetState() {
            return Utils.GetState(player.CurrentState);
        }

        public override void Dispose() {
            Player.Dispose();
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
