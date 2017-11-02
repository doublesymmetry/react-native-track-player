using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Media;
using Windows.Media.Playback;
using Windows.Media.Core;
using Windows.Storage.Streams;

namespace TrackPlayer.Logic {
    class Metadata {

        private MediaManager manager;
        private SystemMediaTransportControls controls;

        public Metadata(MediaManager manager) {
            this.manager = manager;
        }

        public void SetTransportControls(SystemMediaTransportControls transportControls) {
            if(controls != null) {
                controls.IsEnabled = false;
                controls.PlaybackPositionChangeRequested -= OnSeekTo;
                controls.ButtonPressed -= OnButtonPressed;
            }

            controls = transportControls;

            if(controls != null) {
                controls.IsEnabled = true;
                controls.DisplayUpdater.Type = MediaPlaybackType.Music;

                controls.PlaybackPositionChangeRequested += OnSeekTo;
                controls.ButtonPressed += OnButtonPressed;
            }
        }

        public void UpdateOptions(JObject data) {
            Debug.WriteLine("Updating options...");
            JArray capabilities = (JArray)data.GetValue("capabilities");

            controls.IsPlayEnabled = Utils.ContainsInt(capabilities, (int)Capability.Play);
            controls.IsPauseEnabled = Utils.ContainsInt(capabilities, (int)Capability.Pause);
            controls.IsStopEnabled = Utils.ContainsInt(capabilities, (int)Capability.Stop);
            controls.IsPreviousEnabled = Utils.ContainsInt(capabilities, (int)Capability.Previous);
            controls.IsNextEnabled = Utils.ContainsInt(capabilities, (int)Capability.Next);

            // Unsupported for now
            controls.IsChannelDownEnabled = false;
            controls.IsChannelUpEnabled = false;
            controls.IsFastForwardEnabled = false;
            controls.IsRewindEnabled = false;
            controls.IsRecordEnabled = false;
        }

        public void UpdateMetadata(Track track) {
            var display = controls.DisplayUpdater;
            var properties = display.MusicProperties;

            display.AppMediaId = track.id;
            display.Thumbnail = RandomAccessStreamReference.CreateFromUri(track.artwork);

            properties.Title = track.title;
            properties.Artist = track.artist;
            properties.AlbumTitle = track.album;
        }

        public void Dispose() {
            controls.IsEnabled = false;
            controls.PlaybackPositionChangeRequested -= OnSeekTo;
            controls.ButtonPressed -= OnButtonPressed;
            controls = null;
        }

        private void OnSeekTo(SystemMediaTransportControls sender, PlaybackPositionChangeRequestedEventArgs args) {
            JObject obj = new JObject();
            obj.Add("position", args.RequestedPlaybackPosition.TotalSeconds);
            manager.SendEvent(Events.ButtonSeekTo, obj);
        }

        private void OnButtonPressed(SystemMediaTransportControls sender, SystemMediaTransportControlsButtonPressedEventArgs args) {
            string eventType = null;

            switch(args.Button) {
                case SystemMediaTransportControlsButton.Play:
                    eventType = Events.ButtonPlay;
                    break;
                case SystemMediaTransportControlsButton.Pause:
                    eventType = Events.ButtonPause;
                    break;
                case SystemMediaTransportControlsButton.Stop:
                    eventType = Events.ButtonStop;
                    break;
                case SystemMediaTransportControlsButton.Previous:
                    eventType = Events.ButtonSkipPrevious;
                    break;
                case SystemMediaTransportControlsButton.Next:
                    eventType = Events.ButtonSkipNext;
                    break;
                default:
                    return;
            }

            manager.SendEvent(eventType, null);
        }

    }

    enum Capability {
        Unsupported,
        Play,
        Pause,
        Stop,
        Previous,
        Next,
        Seek
    }
}
