#pragma once

#include "pch.h"
#include "NativeModules.h"
#include <string>

namespace winrt::RNTrackPlayer {
    struct Track {
        std::string Url;
        std::string Type;
        std::chrono::seconds Duration;

        std::string Title;
        std::string Artist;
        std::string Album;
        std::string Artwork;

        Track(const React::JSValueObject& data);
        void SetMetadata(const React::JSValueObject& data);
        React::JSValueObject ToObject();
    };

    struct TrackType {
        static constexpr const char* Default = "default";
        static constexpr const char* Dash = "dash";
        static constexpr const char* Hls = "hls";
        static constexpr const char* SmoothStreaming = "smoothstreaming";
    };
}
