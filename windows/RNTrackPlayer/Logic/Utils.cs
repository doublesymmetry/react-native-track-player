using Newtonsoft.Json.Linq;
using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Media.Playback;
using TrackPlayer.Players;

namespace TrackPlayer.Logic {
    class Utils {

        public static T GetValue<T>(JObject obj, string key, T def) {
            JToken val;
            return obj.TryGetValue(key, out val) ? val.ToObject<T>() : def;
        }

        public static MediaPlaybackState GetState(MediaPlaybackState state) {
            if(state == MediaPlaybackState.Opening) {
                return MediaPlaybackState.Buffering;
            }

            return state;
        }

        public static bool IsPlaying(MediaPlaybackState state) {
            return state == MediaPlaybackState.Playing || state == MediaPlaybackState.Buffering;
        }

        public static bool IsPaused(MediaPlaybackState state) {
            return state == MediaPlaybackState.Paused;
        }

        public static Uri GetUri(JObject obj, string key, Uri def) {
            JToken val;
            if(!obj.TryGetValue(key, out val)) return def;

            if(val.Type == JTokenType.Object) {
                return new Uri(((JObject)val).Value<string>("uri"));
            } else if(val.Type == JTokenType.String) {
                return new Uri((string)val);
            } else if(val.Type == JTokenType.Uri) {
                return (Uri)val;
            }

            return def;
        }

        public static bool CheckPlayback(Playback pb, IPromise promise) {
            if(pb == null) {
                promise.Reject("playback", "The playback is not initialized");
                return true;
            }
            return false;
        }

        public static bool ContainsInt(JArray array, int val) {
            for(int i = 0; i < array.Count; i++) {
                JToken token = array[i];
                if(token.Type == JTokenType.Integer && (int)token == val) return true;
            }
            return false;
        }

    }
}
