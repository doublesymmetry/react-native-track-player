using Newtonsoft.Json.Linq;
using ReactNative.Bridge;
using System;
using TrackPlayer.Players;

namespace TrackPlayer.Logic
{
    internal static class Utils
    {
        public static T GetValue<T>(JObject obj, string key, T def)
        {
            return obj.TryGetValue(key, out var val) ? val.ToObject<T>() : def;
        }

        public static bool IsPlaying(PlaybackState state)
        {
            return state == PlaybackState.Playing || state == PlaybackState.Buffering;
        }

        public static bool IsPaused(PlaybackState state)
        {
            return state == PlaybackState.Paused;
        }

        public static Uri GetUri(JObject obj, string key, Uri def)
        {
            if (!obj.TryGetValue(key, out var val)) return def;

            if (val.Type == JTokenType.Object)
                return new Uri(((JObject) val).Value<string>("uri"));
            else if (val.Type == JTokenType.String)
                return new Uri((string) val);
            else if (val.Type == JTokenType.Uri)
                return (Uri) val;

            return def;
        }

        public static bool CheckPlayback(Playback pb, IPromise promise)
        {
            if (pb == null)
            {
                promise.Reject("playback", "The playback is not initialized");
                return true;
            }

            return false;
        }

        public static bool ContainsInt(JArray array, int val)
        {
            for (int i = 0; i < array.Count; i++)
            {
                JToken token = array[i];
                if (token.Type == JTokenType.Integer && (int) token == val) return true;
            }

            return false;
        }

    }
}
