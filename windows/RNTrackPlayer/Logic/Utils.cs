using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Media.Playback;

namespace TrackPlayer.Logic {
    class Utils {

        public static T GetValue<T>(JObject obj, string key, T def) {
            JToken val;
            return obj.TryGetValue(key, val) ? val : def;
        }

        public static MediaPlayerState GetState(MediaPlayerState state) {
            if(state == MediaPlayerState.Opening) {
                return MediaPlayerState.Buffering;
            }

            return state;
        }

        public static bool IsPlaying(MediaPlayerState state) {
            return state == MediaPlayerState.Playing || state == MediaPlayerState.Buffering;
        }

        public static bool IsPaused(MediaPlayerState state) {
            return state == MediaPlayerState.Paused;
        }

        public static Uri GetUri(JObject obj, string key, Uri def) {
            JToken val;
            if(!obj.TryGetValue(key, val)) return def;

            if(val.Type == JTokenType.Object) {
                return new Uri(((JObject)val).Value<string>("uri"));
            } else if(val.Type == JTokenType.String) {
                return new Uri((string)val);
            } else if(val.Type == JTokenType.Uri) {
                return (Uri)val;
            }

            return def;
        }

    }
}
