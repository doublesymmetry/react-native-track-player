#pragma once

#include "pch.h"
#include "NativeModules.h"

namespace winrt::RNTrackPlayer {
    enum class Capability {
        Unsupported = 0,
        Play = 1,
        Pause = 2,
        Stop = 3,
        Previous = 4,
        Next = 5,
        Seek = 6,
        JumpForward = 7,
        JumpBackward = 8
    };

    struct MediaManager;
    struct Track;

    struct Metadata {
        MediaManager* manager;
        winrt::Windows::Media::SystemMediaTransportControls controls;
        double jumpInterval;
        bool play, pause, stop, previous, next, jumpForward, jumpBackward, seek;

        winrt::event_token onSeekToRevoker;
        winrt::event_token onButtonPressedRevoker;

        Metadata(MediaManager* manager);
        ~Metadata();
        void SetTransportControls(winrt::Windows::Media::SystemMediaTransportControls transportControls);
        void UpdateCapabilities();
        void UpdateOptions(React::JSValueObject& data);
        void UpdateMetadata(Track& track);
        void OnSeekTo(winrt::Windows::Media::SystemMediaTransportControls sender,
            winrt::Windows::Media::PlaybackPositionChangeRequestedEventArgs args);
        void OnButtonPressed(winrt::Windows::Media::SystemMediaTransportControls sender,
            winrt::Windows::Media::SystemMediaTransportControlsButtonPressedEventArgs args);

    };
}
