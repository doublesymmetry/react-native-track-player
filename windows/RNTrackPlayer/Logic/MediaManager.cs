using Newtonsoft.Json.Linq;
using ReactNative.Bridge;
using ReactNative.Modules.Core;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Media;
using Windows.Media.Playback;
using Windows.Media.Core;
using TrackPlayer.Players;

namespace TrackPlayer.Logic {
    public class MediaManager {

        private ReactContext context;
        private Metadata metadata;

        private JObject options;
        private Playback player;

        public MediaManager(ReactContext context, JObject options) {
            this.context = context;
            this.options = options;

            this.player = new LocalPlayback(this, options);
            this.metadata = new Metadata(this);
            this.metadata.SetTransportControls(this.player.GetTransportControls());
        }

        public void SendEvent(string eventName, object data) {
            context.GetJavaScriptModule<RCTDeviceEventEmitter>().emit(eventName, data);
        }

        public void UpdateOptions(JObject options) {
            metadata.UpdateOptions(options);
        }

        public Playback GetPlayer() {
            return player;
        }

        public void OnEnd(Track previous, double prevPos) {
            Debug.WriteLine("OnEnd");

            JObject obj = new JObject();
            obj.Add("track", previous?.id);
            obj.Add("position", prevPos);
            SendEvent(Events.PlaybackQueueEnded, obj);
        }

        public void OnStateChange(MediaPlaybackState state) {
            Debug.WriteLine("OnStateChange");

            JObject obj = new JObject();
            obj.Add("state", (int)state);
            SendEvent(Events.PlaybackState, obj);
        }

        public void OnTrackUpdate(Track previous, double prevPos, Track next, bool changed) {
            Debug.WriteLine("OnTrackUpdate");

            metadata.UpdateMetadata(next);

            if(changed) {
                JObject obj = new JObject();
                obj.Add("track", previous?.id);
                obj.Add("position", prevPos);
                obj.Add("nextTrack", next?.id);
                SendEvent(Events.PlaybackTrackChanged, obj);
            }
        }

        public void OnError(string error) {
            Debug.WriteLine("OnError: " + error);

            JObject obj = new JObject();
            obj.Add("error", error);
            SendEvent(Events.PlaybackError, obj);
        }

        public void Dispose() {
            if(player != null) {
                player.Dispose();
                player = null;
            }

            metadata.Dispose();
        }

    }
}
