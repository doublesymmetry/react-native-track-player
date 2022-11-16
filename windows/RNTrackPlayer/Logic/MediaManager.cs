using Newtonsoft.Json.Linq;
using ReactNative.Bridge;
using ReactNative.Modules.Core;
using System.Diagnostics;
using TrackPlayer.Players;

namespace TrackPlayer.Logic
{
    public class MediaManager
    {
        private ReactContext context;
        private Metadata metadata;
        
        private Playback player;

        public MediaManager(ReactContext context)
        {
            this.context = context;
            
            this.metadata = new Metadata(this);
        }

        public void SendEvent(string eventName, object data)
        {
            context.GetJavaScriptModule<RCTDeviceEventEmitter>().emit(eventName, data);
        }

        public void SwitchPlayback(Playback pb)
        {
            if (player != null)
            {
                player.Stop();
                player.Dispose();
            }

            player = pb;
            metadata.SetTransportControls(pb?.GetTransportControls());
        }

        public LocalPlayback CreateLocalPlayback(JObject options)
        {
            return new LocalPlayback(this, options);
        }

        public void UpdateOptions(JObject options)
        {
            metadata.UpdateOptions(options);
        }

        public Playback GetPlayer()
        {
            return player;
        }

        public Metadata GetMetadata()
        {
            return metadata;
        }

        public void OnEnd(Track previous, double prevPos)
        {
            Debug.WriteLine("OnEnd");

            JObject obj = new JObject();
            obj.Add("track", previous?.Id);
            obj.Add("position", prevPos);
            SendEvent(Events.PlaybackQueueEnded, obj);
        }

        public void OnStateChange(PlaybackState state)
        {
            Debug.WriteLine("OnStateChange");

            JObject obj = new JObject();
            obj.Add("state", (int) state);
            SendEvent(Events.PlaybackState, obj);
        }

        public void OnTrackUpdate(Track previous, double prevPos, Track next, bool changed)
        {
            Debug.WriteLine("OnTrackUpdate");

            metadata.UpdateMetadata(next);

            if(changed)
            {
                JObject obj = new JObject();
                obj.Add("track", previous?.Id);
                obj.Add("position", prevPos);
                obj.Add("nextTrack", next?.Id);
                SendEvent(Events.PlaybackTrackChanged, obj);
            }
        }

        public void OnError(string code, string error)
        {
            Debug.WriteLine("OnError: " + error);

            JObject obj = new JObject();
            obj.Add("code", code);
            obj.Add("message", error);
            SendEvent(Events.PlaybackError, obj);
        }

        public void Dispose()
        {
            if(player != null)
            {
                player.Dispose();
                player = null;
            }

            metadata.Dispose();
        }

    }
}
