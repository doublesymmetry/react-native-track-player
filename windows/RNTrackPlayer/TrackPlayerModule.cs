using System;
using Microsoft.ReactNative.Managed;
using System.Collections.Generic;
using TrackPlayer.Logic;
using TrackPlayer.Players;

namespace TrackPlayer
{
    [ReactModule("TrackPlayerModule")]
    public class TrackPlayerModule
    {
        public string Name => "TrackPlayerModule";

        [ReactConstant]
        public int STATE_NONE = (int)PlaybackState.None;

        [ReactConstant]
        public int STATE_PLAYING = (int)PlaybackState.Playing;

        [ReactConstant]
        public int STATE_PAUSED = (int)PlaybackState.Paused;

        [ReactConstant]
        public int STATE_STOPPED = (int)PlaybackState.Stopped;

        [ReactConstant]
        public int STATE_BUFFERING = (int)PlaybackState.Buffering;

        // Capabilities
        [ReactConstant]
        public int CAPABILITY_PLAY = (int)Capability.Play;

        [ReactConstant]
        public int CAPABILITY_PLAY_FROM_ID = (int)Capability.Unsupported;

        [ReactConstant]
        public int CAPABILITY_PLAY_FROM_SEARCH = (int)Capability.Unsupported;

        [ReactConstant]
        public int CAPABILITY_PAUSE = (int)Capability.Pause;

        [ReactConstant]
        public int CAPABILITY_STOP = (int)Capability.Stop;

        [ReactConstant]
        public int CAPABILITY_SEEK_TO = (int)Capability.Seek;

        [ReactConstant]
        public int CAPABILITY_SKIP = (int)Capability.Unsupported;

        [ReactConstant]
        public int CAPABILITY_SKIP_TO_NEXT = (int)Capability.Next;

        [ReactConstant]
        public int CAPABILITY_SKIP_TO_PREVIOUS = (int)Capability.Previous;

        [ReactConstant]
        public int CAPABILITY_SET_RATING = (int)Capability.Unsupported;

        [ReactConstant]
        public int CAPABILITY_JUMP_FORWARD = (int)Capability.JumpForward;

        [ReactConstant]
        public int CAPABILITY_JUMP_BACKWARD = (int)Capability.JumpBackward;

        private MediaManager manager;

        public TrackPlayerModule()
        {
            manager = new MediaManager(this);
        }

        [ReactMethod]
        public void setupPlayer(JSValue options, ReactPromise<JSValue> promise)
        {
            JSValueObject x = JSValueObject.CopyFrom(options.AsObject());
            if (manager.GetPlayer() != null)
            {
                promise.Resolve(JSValue.Null);
                return;
            }

            manager.SwitchPlayback(manager.CreateLocalPlayback(x));
            promise.Resolve(JSValue.Null);
        }

        [ReactMethod]
        public void destroy()
        {
            manager?.SwitchPlayback(null);
        }

        [ReactMethod]
        public void updateOptions(JSValue options, ReactPromise<JSValue> promise)
        {

            JSValueObject x = JSValueObject.CopyFrom(options.AsObject());
            manager.UpdateOptions(x);
            promise.Resolve(JSValue.Null);
        }

        [ReactMethod]
        public void play(ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.Play();
            promise.Resolve(JSValue.Null);
        }

        [ReactMethod]
        public void pause(ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.Pause();
            promise.Resolve(JSValue.Null);
        }

        [ReactMethod]
        public void stop(ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.Stop();
            promise.Resolve(JSValue.Null);
        }

        [ReactMethod]
        public void reset(ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.Reset();
            promise.Resolve(JSValue.Null);
        }

        [ReactMethod]
        public void updateMetadataForTrack(string id, JSValue imetadata, ReactPromise<JSValue> promise)
        {
            JSValueObject metadata = JSValueObject.CopyFrom(imetadata.AsObject());
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            var queue = player.GetQueue();
            var index = queue.FindIndex(t => t.Id == id);

            if (index == -1)
            {
                promise.Reject(new ReactError { Code = "track_not_in_queue", Message = "Track not found" });
            }
            else
            {
                var track = queue[index];
                track.SetMetadata(metadata);
                player.UpdateTrack(index, track);
                promise.Resolve(JSValue.Null);
            }
        }

        [ReactMethod]
        public void removeUpcomingTracks(ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.RemoveUpcomingTracks();
            promise.Resolve(JSValue.Null);
        }

        [ReactMethod]
        public void add(JSValue iarray, JSValue iInsertBeforeId, ReactPromise<JSValue> promise)
        {
            try
            {
                string insertBeforeId = iInsertBeforeId == JSValue.Null ? null : iInsertBeforeId.AsString();
                JSValueArray array = JSValueArray.CopyFrom(iarray.AsArray());
                var player = manager?.GetPlayer();
                if (Utils.CheckPlayback(player, promise)) return;

                List<Track> tracks = new List<Track>(array.Count);

                foreach (JSValue obj in array)
                {
                    tracks.Add(new Track(JSValueObject.CopyFrom(obj.AsObject())));
                }

                player.Add(tracks, insertBeforeId, promise);
                promise.Resolve(JSValue.Null);
            }
            catch (Exception e)
            {
                promise.Reject(new ReactError { Exception = e, Message = e.Message });
            }
        }

        [ReactMethod]
        public void remove(JSValue array, ReactPromise<JSValue> promise)
        {
            var actualArray = array.AsArray();
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            List<string> tracks = new List<string>(actualArray.Count);

            foreach (string id in actualArray)
            {
                tracks.Add(id);
            }

            player.Remove(tracks, promise);
        }

        [ReactMethod]
        public void skip(string track, ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.Skip(track, promise);
        }

        [ReactMethod]
        public void skipToNext(ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.SkipToNext(promise);
        }

        [ReactMethod]
        public void skipToPrevious(ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.SkipToPrevious(promise);
        }

        [ReactMethod]
        public void getQueue(ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            var queue = player.GetQueue();
            var array = new JSValueArray();

            foreach (var track in queue)
            {
                array.Add(track.ToObject());
            }

            promise.Resolve(array);
        }

        [ReactMethod]
        public void getCurrentTrack(ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetCurrentTrack()?.Id);
        }

        [ReactMethod]
        public void getTrack(string id, ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetTrack(id)?.ToObject());
        }

        [ReactMethod]
        public void getVolume(ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetVolume());
        }

        [ReactMethod]
        public void setVolume(double volume, ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.SetVolume(volume);
            promise.Resolve(JSValue.Null);
        }

        [ReactMethod]
        public void getRate(ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetRate());
        }

        [ReactMethod]
        public void setRate(double rate, ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.SetRate(rate);
            promise.Resolve(JSValue.Null);
        }

        [ReactMethod]
        public void seekTo(double seconds, ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.SeekTo(seconds);
            promise.Resolve(JSValue.Null);
        }

        [ReactMethod]
        public void getPosition(ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetPosition());
        }

        [ReactMethod]
        public void getBufferedPosition(ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetBufferedPosition());
        }

        [ReactMethod]
        public void getDuration(ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetDuration());
        }

        [ReactMethod]
        public void getState(ReactPromise<JSValue> promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetState().ToString());
        }

        [ReactEvent("remote-play")]
        public Action<JSValue> ButtonPlay { get; set; }

        [ReactEvent("remote-pause")]
        public Action<JSValue> ButtonPause { get; set; }

        [ReactEvent("remote-stop")]
        public Action<JSValue> ButtonStop { get; set; }

        [ReactEvent("remote-next")]
        public Action<JSValue> ButtonSkipNext { get; set; }

        [ReactEvent("remote-previous")]
        public Action<JSValue> ButtonSkipPrevious { get; set; }

        [ReactEvent("remote-seek")]
        public Action<JSValue> ButtonSeekTo { get; set; }

        [ReactEvent("remote-jump-forward")]
        public Action<JSValue> ButtonJumpForward { get; set; }

        [ReactEvent("remote-jump-backward")]
        public Action<JSValue> ButtonJumpBackward { get; set; }

        [ReactEvent("playback-state")]
        public Action<JSValue> PlaybackStateAction { get; set; }

        [ReactEvent("playback-track-changed")]
        public Action<JSValue> PlaybackTrackChanged { get; set; }

        [ReactEvent("playback-queue-ended")]
        public Action<JSValue> PlaybackQueueEnded { get; set; }

        [ReactEvent("playback-error")]
        public Action<JSValue> PlaybackError { get; set; }
    }
}
