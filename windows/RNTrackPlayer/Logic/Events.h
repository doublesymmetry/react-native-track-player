#pragma once

#include "pch.h"
#include "NativeModules.h"

namespace winrt::RNTrackPlayer {
    struct Events {
        static const char* ButtonPlay;
        static const char* ButtonPause;
        static const char* ButtonStop;
        static const char* ButtonSkipNext;
        static const char* ButtonSkipPrevious;
        static const char* ButtonSeekTo;
        static const char* ButtonJumpForward;;
        static const char* ButtonJumpBackward;

        static const char* PlaybackState;
        static const char* PlaybackTrackChanged;
        static const char* PlaybackQueueEnded;
        static const char* PlaybackError;
    };
}