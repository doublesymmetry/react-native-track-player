#pragma once

#include "pch.h"
#include "NativeModules.h"
#include "Logic/Metadata.h"
#include "Logic/Track.h"
#include "Players/Playback.h"
#include "Players/LocalPlayback.h"

namespace winrt::RNTrackPlayer {
    struct MediaManager {
    private:
        React::ReactContext context;
        Metadata metadata;

        Playback* player;

    public:
        MediaManager(React::ReactContext const& context);
        void SendEvent(const std::string& eventName, const JSValueObject& data);
        void SwitchPlayback(Playback* pb);
        LocalPlayback* CreateLocalPlayback(React::JSValueObject& options);
        void UpdateOptions(React::JSValueObject& options);
        Playback* GetPlayer() const;
        Metadata* GetMetadata() const;
        void OnEnd(int previous, double prevPos);
        void OnStateChange(PlaybackState state);
        void OnTrackUpdate(int previousIndex, double prevPos, int nextIndex, Track* next);
        void OnError(const std::string& code, const std::string& error);
    };
}
