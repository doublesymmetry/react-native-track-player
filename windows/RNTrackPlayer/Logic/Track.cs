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

        public string title;
        public string artist;
        public string album;
        public string[] genre;
        public Uri artwork;

        public Track(JObject data) {
            this.id = data.GetValue<string>("id");
            this.url = Utils.GetUri("url", null);
            this.duration = Utils.GetValue<double>("duration", 0);
            this.title = Utils.GetValue<string>("title", null);
            this.artist = Utils.GetValue<string>("artist", null);
            this.album = Utils.GetValue<string>("album", null);
            this.genre = Utils.GetValue<string>("genre", null).Split(",");
            this.artwork = Utils.GetUri("artwork", null);
        }

        public JObject ToObject() {
            return JObject.FromObject(this);
        }

    }
}
