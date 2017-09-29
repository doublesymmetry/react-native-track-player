using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TrackPlayer.Logic {

    enum Events {

        ButtonPlay = "remote-play",
        ButtonPause = "remote-pause",
        ButtonStop = "remote-stop",
        ButtonSkipNext = "remote-next",
        ButtonSkipPrevious = "remote-previous",
        ButtonSeekTo = "remote-seek",

        PlaybackState = "playback-state",
        PlaybackTrackChanged = "playback-track-changed",
        PlaybackQueueEnded = "playback-queue-ended",
        PlaybackError = "playback-error",

        // Cast Events - Unused for now
        CastState = "cast-state",
        CastConnecting = "cast-connecting",
        CastConnected = "cast-connected",
        CastConnectionFailed = "cast-connection-failed",
        CastDisconnecting = "cast-disconnecting",
        CastDisconnected = "cast-disconnected"

    }

}
