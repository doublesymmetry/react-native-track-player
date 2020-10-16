using Microsoft.ReactNative.Managed;
using System;
using TrackPlayer.Players;

namespace TrackPlayer.Logic
{
    internal static class Utils
    {

        public static T GetValue<T>(JSValueObject obj, string key, T def)
        {
            return obj.TryGetValue(key, out var val) ? val.To<T>() : def;
        }

        public static bool IsPlaying(PlaybackState state)
        {
            return state == PlaybackState.Playing || state == PlaybackState.Buffering;
        }

        public static bool IsPaused(PlaybackState state)
        {
            return state == PlaybackState.Paused;
        }

        public static Uri GetUri(JSValueObject obj, string key, Uri def)
        {
            try
            {
                var val = obj[key];
                if (val.TryGetString(out string s))
                {
                    return new Uri(s);
                }
                else
                {
                    return def;
                }
            }
            catch (Exception e)
            {
                return def;
            }
        }

        public static bool CheckPlayback(Playback pb, ReactPromise<JSValue> promise)
        {
            if (pb == null)
            {
                promise.Reject(new ReactError { Code = "playback", Message = "The playback is not initialized" });
                return true;
            }

            return false;
        }

        public static bool ContainsInt(JSValueArray array, int val)
        {
            for (int i = 0; i < array.Count; i++)
            {
                array[i].TryGetInt64(out long token);
                if (token == val) return true;
            }

            return false;
        }
    }
}
