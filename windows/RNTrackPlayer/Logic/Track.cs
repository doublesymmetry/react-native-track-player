using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TrackPlayer.Logic {
    public struct Track {

        public string id;
        public Uri url;

        public double duration;
        public TrackType type;

        public string title;
        public string artist;
        public string album;
        public List<string> genre;

        public Uri artwork;

        public Track(JObject data) {
            this.id = data.GetValue("id");
            this.url = data.GetValue("url");//TODO handle require
            this.duration = Utils.GetValue("duration", 0);
            this.type = Utils.GetValue("type", TrackType.Default);
            this.title = Utils.GetValue("title", null);
            this.artist = Utils.GetValue("artist", null);
            this.album = Utils.GetValue("album", null);
            this.genre = Utils.GetValue("genre", null);//TODO handle arrays
            this.artwork = Utils.GetValue("artwork", null); // TODO handle require
        }

        public JObject ToObject() {
            return new JObject(this);
        }

    }

    enum TrackType {
        Default = "default",
        DASH = "dash",
        HLS = "hls",
        SmoothStreaming = "smoothstreaming"
    }
}
