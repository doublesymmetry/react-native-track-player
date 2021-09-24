#pragma once

#include "pch.h"
#include "NativeModules.h"

using namespace winrt::Microsoft::ReactNative;

namespace winrt::RNTrackPlayer {
    enum class PlaybackState : int {
        None = 0,
        Playing = 1,
        Paused = 2,
        Buffering = 3,
        Stopped = 4
    };

    struct MediaManager;
    struct Track;

    struct Playback {
    protected:
        MediaManager& manager;

        std::vector<Track> queue;
        PlaybackState prevState{ PlaybackState::None };

    public:
        int currentTrack{ -1 };
        Playback(MediaManager& manager);
        virtual ~Playback();
        void UpdateState(PlaybackState state);
        void UpdateCurrentTrack(size_t index, React::ReactPromise<JSValue>* promise);
        Track* GetCurrentTrack();
        std::vector<Track>& GetQueue();
        void Add(std::vector<Track>& tracks, int insertBeforeIndex,
            React::ReactPromise<JSValue>& promise);
        void Remove(std::vector<int> indexes, React::ReactPromise<JSValue>& promise);
        void UpdateTrack(size_t index, Track& track);
        virtual winrt::Windows::Media::SystemMediaTransportControls GetTransportControls() = 0;
        virtual void Load(Track& track, React::ReactPromise<JSValue>* promise) = 0;
        virtual void Play() = 0;
        virtual void Pause() = 0;
        virtual void Stop() = 0;
        void Reset();
        void RemoveUpcomingTracks();
        void Skip(int index, React::ReactPromise<JSValue>& promise);
        bool HasNext();
        void SkipToNext(React::ReactPromise<JSValue>& promise);
        void SkipToPrevious(React::ReactPromise<JSValue>& promise);
        virtual void SetVolume(double volume) = 0;
        virtual double GetVolume() = 0;
        virtual void SetRate(double rate) = 0;
        virtual double GetRate() = 0;
        virtual void SeekTo(double seconds) = 0;
        virtual double GetPosition() = 0;
        virtual double GetBufferedPosition() = 0;
        virtual double GetDuration() = 0;
        virtual PlaybackState GetState() = 0;
    };
}