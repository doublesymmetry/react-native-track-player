#pragma once

#include "pch.h"
#include "NativeModules.h"

namespace winrt::RNTrackPlayer {
    struct Events {
        static constexpr const char* ButtonPlay = "remote-play";
        static constexpr const char* ButtonPause = "remote-pause";
        static constexpr const char* ButtonStop = "remote-stop";
        static constexpr const char* ButtonSkipNext = "remote-next";
        static constexpr const char* ButtonSkipPrevious = "remote-previous";
        static constexpr const char* ButtonSeekTo = "remote-seek";
        static constexpr const char* ButtonJumpForward = "remote-jump-forward";
        static constexpr const char* ButtonJumpBackward = "remote-jump-backward";

        static constexpr const char* PlaybackState = "playback-state";
        static constexpr const char* PlaybackTrackChanged = "playback-track-changed";
        static constexpr const char* PlaybackQueueEnded = "playback-queue-ended";
        static constexpr const char* PlaybackError = "playback-error";
    };
}