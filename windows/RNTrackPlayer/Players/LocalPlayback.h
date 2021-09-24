#pragma once

#include "pch.h"
#include "NativeModules.h"
#include "Players/Playback.h"

namespace winrt::RNTrackPlayer {
    struct LocalPlayback : public Playback {
        winrt::Windows::Media::Playback::MediaPlayer player;

        React::ReactPromise<JSValue> loadCallback;
        bool hasLoadCallback{ false };

        bool started{ false };
        bool ended{false};
        double startPos{ 0 };

        LocalPlayback(MediaManager& manager, React::JSValueObject& options);
        virtual winrt::Windows::Media::SystemMediaTransportControls GetTransportControls() override;
        virtual void Load(Track& track, React::ReactPromise<JSValue>* promise) override;
        virtual void Play() override;
        virtual void Pause() override;
        virtual void Stop() override;
        virtual void SetVolume(double volume) override;
        virtual double GetVolume() override;
        virtual void SetRate(double rate) override;
        virtual double GetRate() override;
        virtual void SeekTo(double seconds) override;
        virtual double GetPosition() override;
        virtual double GetBufferedPosition() override;
        virtual double GetDuration() override;
        virtual PlaybackState GetState() override;

    private:
        void OnStateChange(winrt::Windows::Media::Playback::MediaPlayer sender,
            const winrt::Windows::Foundation::IInspectable& args);
        void OnEnd(winrt::Windows::Media::Playback::MediaPlayer sender,
            const winrt::Windows::Foundation::IInspectable& args);
        void OnError(winrt::Windows::Media::Playback::MediaPlayer sender,
            winrt::Windows::Media::Playback::MediaPlayerFailedEventArgs args);
        void OnLoad(winrt::Windows::Media::Playback::MediaPlayer sender,
            const winrt::Windows::Foundation::IInspectable& args);
    };
}
