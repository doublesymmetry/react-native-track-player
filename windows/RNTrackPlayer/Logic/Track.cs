using Newtonsoft.Json.Linq;
using System;

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

        private JObject _originalObj;

        public Track(JObject data)
        {
            Id = (string)data.GetValue("id");
            Url = Utils.GetUri(data, "url", null);
            Type = Utils.GetValue<string>(data, "type", TrackType.Default);

            SetMetadata(data);

            _originalObj = data;
        }

        public void SetMetadata(JObject data)
        {
            Duration = Utils.GetValue<double>(data, "duration", 0);

            Title = Utils.GetValue<string>(data, "title", null);
            Artist = Utils.GetValue<string>(data, "artist", null);
            Album = Utils.GetValue<string>(data, "album", null);
            Artwork = Utils.GetUri(data, "artwork", null);

            if (_originalObj != null && _originalObj != data)
                _originalObj.Merge(data);
        }

        public JObject ToObject()
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
