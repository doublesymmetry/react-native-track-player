using Newtonsoft.Json.Linq;
using ReactNative.Bridge;
using System.Collections.Generic;
using TrackPlayer.Logic;
using TrackPlayer.Players;
using System.Threading.Tasks;

namespace TrackPlayer
{
    class TrackPlayerModule : ReactContextNativeModuleBase
    {
        public override string Name => "TrackPlayerModule";

        public override JObject ModuleConstants {
            get {
                var obj = new JObject();

                // States
                obj["STATE_NONE"] = (int) PlaybackState.None;
                obj["STATE_PLAYING"] = (int) PlaybackState.Playing;
                obj["STATE_PAUSED"] = (int) PlaybackState.Paused;
                obj["STATE_STOPPED"] = (int) PlaybackState.Stopped;
                obj["STATE_BUFFERING"] = (int) PlaybackState.Buffering;

                // Capabilities
                obj["CAPABILITY_PLAY"] = (int) Capability.Play;
                obj["CAPABILITY_PLAY_FROM_ID"] = (int) Capability.Unsupported;
                obj["CAPABILITY_PLAY_FROM_SEARCH"] = (int) Capability.Unsupported;
                obj["CAPABILITY_PAUSE"] = (int) Capability.Pause;
                obj["CAPABILITY_STOP"] = (int) Capability.Stop;
                obj["CAPABILITY_SEEK_TO"] = (int) Capability.Seek;
                obj["CAPABILITY_SKIP"] = (int) Capability.Unsupported;
                obj["CAPABILITY_SKIP_TO_NEXT"] = (int) Capability.Next;
                obj["CAPABILITY_SKIP_TO_PREVIOUS"] = (int) Capability.Previous;
                obj["CAPABILITY_SET_RATING"] = (int) Capability.Unsupported;
                obj["CAPABILITY_JUMP_FORWARD"] = (int) Capability.JumpForward;
                obj["CAPABILITY_JUMP_BACKWARD"] = (int) Capability.JumpBackward;

                return obj;
            }
        }

        private MediaManager manager;

        public TrackPlayerModule(ReactContext reactContext) : base(reactContext)
        {

        }

        public override void Initialize()
        {
            manager = new MediaManager(Context);
            base.Initialize();
        }

        public override async Task OnReactInstanceDisposeAsync()
        {
            if (manager != null)
            {
                manager.Dispose();
                manager = null;
            }

            await base.OnReactInstanceDisposeAsync();
        }

        [ReactMethod]
        public void setupPlayer(JObject options, IPromise promise)
        {
            if (manager.GetPlayer() != null)
            {
                promise.Resolve(null);
                return;
            }

            manager.SwitchPlayback(manager.CreateLocalPlayback(options));
            promise.Resolve(null);
        }

        [ReactMethod]
        public void destroy()
        {
            manager?.SwitchPlayback(null);
        }

        [ReactMethod]
        public void updateOptions(JObject options, IPromise promise)
        {
            manager.UpdateOptions(options);
            promise.Resolve(null);
        }

        [ReactMethod]
        public void play(IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.Play();
            promise.Resolve(null);
        }

        [ReactMethod]
        public void pause(IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.Pause();
            promise.Resolve(null);
        }

        [ReactMethod]
        public void stop(IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.Stop();
            promise.Resolve(null);
        }

        [ReactMethod]
        public void reset(IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.Reset();
            promise.Resolve(null);
        }

        [ReactMethod]
        public void updateMetadataForTrack(string id, JObject metadata, IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            var queue = player.GetQueue();
            var index = queue.FindIndex(t => t.Id == id);

            if (index == -1)
            {
                promise.Reject("track_not_in_queue", "Track not found");
            }
            else
            {
                var track = queue[index];
                track.SetMetadata(metadata);
                player.UpdateTrack(index, track);
                promise.Resolve(null);
            }
        }

        [ReactMethod]
        public void removeUpcomingTracks(IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.RemoveUpcomingTracks();
            promise.Resolve(null);
        }

        [ReactMethod]
        public void add(JArray array, string insertBeforeId, IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            List<Track> tracks = new List<Track>(array.Count);

            foreach (JObject obj in array)
            {
                tracks.Add(new Track(obj));
            }

            player.Add(tracks, insertBeforeId, promise);
        }

        [ReactMethod]
        public void remove(JArray array, IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            List<string> tracks = new List<string>(array.Count);

            foreach (string id in array)
            {
                tracks.Add(id);
            }

            player.Remove(tracks, promise);
        }

        [ReactMethod]
        public void skip(string track, IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.Skip(track, promise);
        }

        [ReactMethod]
        public void skipToNext(IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.SkipToNext(promise);
        }

        [ReactMethod]
        public void skipToPrevious(IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.SkipToPrevious(promise);
        }

        [ReactMethod]
        public void getQueue(IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            var queue = player.GetQueue();
            var array = new JArray();
            
            foreach(var track in queue)
            {
                array.Add(track.ToObject());
            }
            
            promise.Resolve(array);
        }

        [ReactMethod]
        public void getCurrentTrack(IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;
            
            promise.Resolve(player.GetCurrentTrack()?.Id);
        }

        [ReactMethod]
        public void getTrack(string id, IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetTrack(id)?.ToObject());
        }

        [ReactMethod]
        public void getVolume(IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetVolume());
        }

        [ReactMethod]
        public void setVolume(double volume, IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.SetVolume(volume);
            promise.Resolve(null);
        }

        [ReactMethod]
        public void getRate(IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetRate());
        }

        [ReactMethod]
        public void setRate(double rate, IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.SetRate(rate);
            promise.Resolve(null);
        }

        [ReactMethod]
        public void seekTo(double seconds, IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            player.SeekTo(seconds);
            promise.Resolve(null);
        }

        [ReactMethod]
        public void getPosition(IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetPosition());
        }

        [ReactMethod]
        public void getBufferedPosition(IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetBufferedPosition());
        }

        [ReactMethod]
        public void getDuration(IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetDuration());
        }

        [ReactMethod]
        public void getState(IPromise promise)
        {
            var player = manager?.GetPlayer();
            if (Utils.CheckPlayback(player, promise)) return;

            promise.Resolve(player.GetState());
        }
    }
}
