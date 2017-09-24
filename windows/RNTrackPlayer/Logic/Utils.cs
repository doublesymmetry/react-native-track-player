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

    }
}
