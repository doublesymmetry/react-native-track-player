#pragma once

#include "pch.h"
#include "NativeModules.h"
#include <string>

namespace winrt::RNTrackPlayer {
    struct Track {
        std::string Id;
        std::string Url;
        std::string Type;
        double Duration;

        std::string Title;
        std::string Artist;
        std::string Album;
        std::string Artwork;

        Track(const React::JSValueObject& data);
        void SetMetadata(const React::JSValueObject& data);
        React::JSValueObject ToObject();
    };

    struct TrackType {
        static const char* Default;
        static const char* Dash;
        static const char* Hls;
        static const char* SmoothStreaming;
    };
}