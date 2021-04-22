#pragma once

#include "pch.h"
#include "NativeModules.h"
#include "Logic/Metadata.h"
#include "Logic/Track.h"
#include "Players/Playback.h"
#include "Players/LocalPlayback.h"

namespace winrt::RNTrackPlayer {
    struct MediaManager {
        React::ReactContext context;
        Metadata metadata;

        Playback* player;

        MediaManager(React::ReactContext const& context);
        void SendEvent(std::string eventName, const JSValueObject& data);
        void SwitchPlayback(Playback* pb);
        LocalPlayback* CreateLocalPlayback(React::JSValueObject& options);
        void UpdateOptions(React::JSValueObject& options);
        Playback* GetPlayer();
        Metadata* GetMetadata();
        void OnEnd(Track* previous, double prevPos);
        void OnStateChange(PlaybackState state);
        void OnTrackUpdate(Track* previous, double prevPos, Track* next, bool changed);
        void OnError(std::string code, std::string error);
    };
}