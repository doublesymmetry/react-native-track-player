using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TrackPlayer.Logic {

    class Events {

        public static readonly string ButtonPlay = "remote-play";
        public static readonly string ButtonPause = "remote-pause";
        public static readonly string ButtonStop = "remote-stop";
        public static readonly string ButtonSkipNext = "remote-next";
        public static readonly string ButtonSkipPrevious = "remote-previous";
        public static readonly string ButtonSeekTo = "remote-seek";

        public static readonly string PlaybackState = "playback-state";
        public static readonly string PlaybackTrackChanged = "playback-track-changed";
        public static readonly string PlaybackQueueEnded = "playback-queue-ended";
        public static readonly string PlaybackError = "playback-error";

        // Cast Events - Unused for now
        public static readonly string CastState = "cast-state";
        public static readonly string CastConnecting = "cast-connecting";
        public static readonly string CastConnected = "cast-connected";
        public static readonly string CastConnectionFailed = "cast-connection-failed";
        public static readonly string CastDisconnecting = "cast-disconnecting";
        public static readonly string CastDisconnected = "cast-disconnected";

    }

}
