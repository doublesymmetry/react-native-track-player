using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TrackPlayer.Logic {

    enum Events {

        ButtonPlay = "play",
        ButtonPause = "pause",
        ButtonStop = "stop",
        ButtonSkipNext = "skipToNext",
        ButtonSkipPrevious = "skipToPrevious",
        ButtonSeekTo = "seekTo",

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
