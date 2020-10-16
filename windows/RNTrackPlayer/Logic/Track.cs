using Microsoft.ReactNative.Managed;
using System;
using System.Diagnostics;

namespace TrackPlayer.Logic
{
    public class Track
    {
        public string Id { get; set; }
        public Uri Url { get; set; }
        public string Type { get; set; }
        public double Duration { get; set; }
        public string Title { get; set; }
        public string Artist { get; set; }
        public string Album { get; set; }
        public Uri Artwork { get; set; }

        private JSValueObject _originalObj;

        public Track(JSValueObject data)
        {
            Id = Utils.GetValue<string>(data, "id", null);
            Url = Utils.GetUri(data, "url", null);
            Type = Utils.GetValue<string>(data, "type", TrackType.Default);

            SetMetadata(data);

            _originalObj = data;
        }

        public void SetMetadata(JSValueObject data)
        {
            Title = Utils.GetValue<string>(data, "title", null);
            Artist = Utils.GetValue<string>(data, "artist", null);
            Album = Utils.GetValue<string>(data, "album", null);
            Artwork = Utils.GetUri(data, "artwork", null);
            Debug.WriteLine("Track.cs - implement merge of orig object");
        }

        public JSValueObject ToObject()
        {
            return _originalObj;
        }
    }

    public static class TrackType
    {
        public const string Default = "default";
        public const string Dash = "dash";
        public const string Hls = "hls";
        public const string SmoothStreaming = "smoothstreaming";
    }
}
