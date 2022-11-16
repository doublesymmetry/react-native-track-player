using Newtonsoft.Json.Linq;
using System;
using System.Diagnostics;
using ReactNative.Bridge;
using Windows.Media;
using Windows.Media.Playback;
using Windows.Media.Core;
using TrackPlayer.Logic;

namespace TrackPlayer.Players
{
    public class LocalPlayback : Playback
    {
        private MediaPlayer player;

        private IPromise loadCallback;

        private bool started = false;
        private bool ended = false;
        private double startPos = 0;

        public LocalPlayback(MediaManager manager, JObject options) : base(manager)
        {
            player = new MediaPlayer();
            player.AutoPlay = false;
            player.AudioCategory = MediaPlayerAudioCategory.Media;
            player.CommandManager.IsEnabled = false;

            player.MediaOpened += OnLoad;
            player.MediaFailed += OnError;
            player.MediaEnded += OnEnd;
            player.CurrentStateChanged += OnStateChange;
        }

        public override SystemMediaTransportControls GetTransportControls()
        {
            return player.SystemMediaTransportControls;
        }

        protected override void Load(Track track, IPromise promise)
        {
            started = false;
            ended = false;
            startPos = 0;
            loadCallback = promise;

            player.Source = MediaSource.CreateFromUri(track.Url);
        }

        public override void Play()
        {
            started = true;
            ended = false;
            player.Play();
        }

        public override void Pause()
        {
            started = false;
            player.Pause();
        }

        public override void Stop()
        {
            started = false;
            ended = true;
            player.Pause();
            player.PlaybackSession.Position = TimeSpan.FromSeconds(0);
        }

        public override void SetVolume(double volume)
        {
            player.Volume = volume;
        }

        public override double GetVolume()
        {
            return player.Volume;
        }

        public override void SetRate(double rate)
        {
            player.PlaybackSession.PlaybackRate = rate;
        }

        public override double GetRate()
        {
            return player.PlaybackSession.PlaybackRate;
        }

        public override void SeekTo(double seconds)
        {
            startPos = seconds;
            player.PlaybackSession.Position = TimeSpan.FromSeconds(seconds);
        }

        public override double GetPosition()
        {
            return player.PlaybackSession.Position.TotalSeconds;
        }

        public override double GetBufferedPosition()
        {
            try
            {
                return player.PlaybackSession.BufferingProgress * GetDuration();
            }
            catch (Exception)
            {
                return 0;
            }
        }

        public override double GetDuration()
        {
            double duration = player.PlaybackSession.NaturalDuration.TotalSeconds;

            if (duration <= 0)
            {
                Track track = GetCurrentTrack();
                duration = track != null && track.Duration > 0 ? track.Duration : 0;
            }

            return duration;
        }

        public override PlaybackState GetState()
        {
            MediaPlaybackState state = player.PlaybackSession.PlaybackState;

            if (ended && (state == MediaPlaybackState.Paused || state == MediaPlaybackState.None))
                return PlaybackState.Stopped;
            else if (state == MediaPlaybackState.Opening || state == MediaPlaybackState.Buffering)
                return PlaybackState.Buffering;
            else if (state == MediaPlaybackState.None)
                return PlaybackState.None;
            else if (state == MediaPlaybackState.Paused)
                return PlaybackState.Paused;
            else if (state == MediaPlaybackState.Playing)
                return PlaybackState.Playing;

            return PlaybackState.None;
        }

        public override void Dispose()
        {
            player.Dispose();
        }

        private void OnStateChange(MediaPlayer sender, object args)
        {
            UpdateState(GetState());
        }

        private void OnEnd(MediaPlayer sender, object args)
        {
            if (HasNext())
            {
                UpdateCurrentTrack(currentTrack + 1, null);
                Play();
            }
            else
            {
                manager.OnEnd(GetCurrentTrack(), GetPosition());
            }
        }

        private void OnError(MediaPlayer sender, MediaPlayerFailedEventArgs args)
        {
            loadCallback?.Reject("error", args.ErrorMessage);
            loadCallback = null;

            Debug.WriteLine(args.Error);
            Debug.WriteLine(args.ErrorMessage);

            string code = "playback";

            if (args.Error == MediaPlayerError.DecodingError || args.Error == MediaPlayerError.SourceNotSupported)
                code = "playback-renderer";
            else if (args.Error == MediaPlayerError.NetworkError)
                code = "playback-source";

            manager.OnError(code, args.ErrorMessage);
        }

        private void OnLoad(MediaPlayer sender, object args)
        {
            Debug.WriteLine("OnLoad");

            if (startPos > 0)
            {
                player.PlaybackSession.Position = TimeSpan.FromSeconds(startPos);
                startPos = 0;
            }

            if (started) Play();

            loadCallback?.Resolve(null);
            loadCallback = null;
        }
    }
}
