using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TrackPlayer.Logic {
    public class Track {

        public string id;
        public Uri url;
        public bool adaptive;
        public double duration;

        public string title;
        public string artist;
        public string album;
        public Uri artwork;

        public Track(JObject data) {
            this.id = (string)data.GetValue("id");
            this.url = Utils.GetUri(data, "url", null);
            this.adaptive = Utils.GetValue<string>(data, "type", "default") != "default";
            this.duration = Utils.GetValue<double>(data, "duration", 0.0);
            this.title = Utils.GetValue<string>(data, "title", null);
            this.artist = Utils.GetValue<string>(data, "artist", null);
            this.album = Utils.GetValue<string>(data, "album", null);
            this.artwork = Utils.GetUri(data, "artwork", null);
        }

        public JObject ToObject() {
            return JObject.FromObject(this);
        }

    }
}
