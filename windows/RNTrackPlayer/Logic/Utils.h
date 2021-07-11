#pragma once

#include "pch.h"
#include "NativeModules.h"

using namespace winrt::Microsoft::ReactNative;

namespace winrt::RNTrackPlayer {
    struct Playback;
    enum class PlaybackState : int;

    struct Utils {
        static bool IsPlaying(PlaybackState state);
        static bool IsPaused(PlaybackState state);
        static std::string GetValue(const JSValueObject& obj, const std::string& key, const std::string& def);
        static winrt::Windows::Foundation::Uri GetUri(const React::JSValueObject& obj,
            const std::string& key, winrt::Windows::Foundation::Uri def);
        static bool CheckPlayback(Playback* pb, ReactPromise<JSValue>& promise);
        static bool ContainsInt(const React::JSValueArray& array, int val);
    };
}