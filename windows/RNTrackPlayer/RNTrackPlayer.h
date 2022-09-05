#pragma once

#include "pch.h"
#include "NativeModules.h"
#include "Logic/MediaManager.h"

using namespace winrt::Microsoft::ReactNative;

namespace winrt::RNTrackPlayer {
  REACT_MODULE(TrackPlayerModule);
  struct TrackPlayerModule {
  private:
    std::unique_ptr<MediaManager> manager;

  public:
    REACT_INIT(Init);
    void Init(React::ReactContext const& reactContext) noexcept;

    REACT_CONSTANT_PROVIDER(GetConstantProvider)
    void GetConstantProvider(ReactConstantProvider& provider) noexcept;

    REACT_METHOD(SetupPlayer, L"setupPlayer")
    void SetupPlayer(JSValueObject options, ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(Destroy, L"destroy")
    void Destroy() noexcept;

    REACT_METHOD(UpdateOptions, L"updateOptions")
    void UpdateOptions(JSValueObject options, ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(Play, L"play")
    void Play(ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(Pause, L"pause")
    void Pause(ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(Stop, L"stop")
    void Stop(ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(Reset, L"reset")
    void Reset(ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(UpdateMetadataForTrack, L"updateMetadataForTrack")
    void UpdateMetadataForTrack(const int index, JSValueObject metadata,
        ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(RemoveUpcomingTracks, L"removeUpcomingTracks")
    void RemoveUpcomingTracks(ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(Add, L"add")
    void Add(JSValueArray arr, int insertBeforeIndex,
        ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(Remove, L"remove")
    void Remove(JSValueArray arr, ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(Skip, L"skip")
    void Skip(const int trackId, double initialTime, ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(SkipToNext, L"skipToNext")
    void SkipToNext(double initialTime, ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(SkipToPrevious, L"skipToPrevious")
    void SkipToPrevious(double initialTime, ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(GetQueue, L"getQueue")
    void GetQueue(ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(GetCurrentTrack, L"getCurrentTrack")
    void GetCurrentTrack(ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(GetTrack, L"getTrack")
    void GetTrack(const int index, ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(GetVolume, L"getVolume")
    void GetVolume(ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(SetVolume, L"setVolume")
    void SetVolume(double volume,  ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(GetRate, L"getRate")
    void GetRate(ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(SetRate, L"setRate")
    void SetRate(double rate, ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(SeekTo, L"seekTo")
    void SeekTo(double seconds, ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(GetPosition, L"getPosition")
    void GetPosition(ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(GetBufferedPosition, L"getBufferedPosition")
    void GetBufferedPosition(ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(GetDuration, L"getDuration")
    void GetDuration(ReactPromise<JSValue> promise) noexcept;

    REACT_METHOD(GetState, L"getState")
    void GetState(ReactPromise<JSValue> promise) noexcept;
  };
}
