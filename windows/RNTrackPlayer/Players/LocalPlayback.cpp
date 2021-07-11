#include "pch.h"
#include "Logic/MediaManager.h"
#include "Logic/Track.h"
#include "Players/LocalPlayback.h"

using namespace winrt::RNTrackPlayer;
using namespace winrt::Windows::Media::Core;
using namespace winrt::Windows::Media::Playback;

LocalPlayback::LocalPlayback(MediaManager& manager, React::JSValueObject&)
    : Playback(manager),
      loadCallback(IJSValueWriter(), MethodResultCallback(), MethodResultCallback())
{
    player = MediaPlayer();
    player.AutoPlay(false);
    player.AudioCategory(MediaPlayerAudioCategory::Media);
    player.CommandManager().IsEnabled(false);

    player.MediaOpened({ this, &LocalPlayback::OnLoad });
    player.MediaFailed({ this, &LocalPlayback::OnError });
    player.MediaEnded({ this, &LocalPlayback::OnEnd });
    player.CurrentStateChanged({ this, &LocalPlayback::OnStateChange });
}

winrt::Windows::Media::SystemMediaTransportControls LocalPlayback::GetTransportControls()
{
    return player.SystemMediaTransportControls();
}

void LocalPlayback::Load(Track& track, React::ReactPromise<JSValue>* promise)
{
    started = false;
    ended = false;
    startPos = 0;

    if (promise)
    {
        new(&loadCallback) ReactPromise<JSValue>(*promise);
        hasLoadCallback = true;
    }

    Uri uri(winrt::to_hstring(track.Url.c_str()));
    player.Source(MediaSource::CreateFromUri(uri));
}

void LocalPlayback::Play()
{
    started = true;
    ended = false;
    player.Play();
}

void LocalPlayback::Pause()
{
    started = false;
    player.Pause();
}

void LocalPlayback::Stop()
{
    started = false;
    ended = true;
    player.Pause();
    player.PlaybackSession().Position(TimeSpan::zero());
}

void LocalPlayback::SetVolume(double volume)
{
    player.Volume(volume);
}

double LocalPlayback::GetVolume()
{
    return player.Volume();
}

void LocalPlayback::SetRate(double rate)
{
    player.PlaybackSession().PlaybackRate(rate);
}

double LocalPlayback::GetRate()
{
    return player.PlaybackSession().PlaybackRate();
}

void LocalPlayback::SeekTo(double seconds)
{
    startPos = seconds;
    player.PlaybackSession().Position(std::chrono::seconds(static_cast<int>(seconds)));
}

double LocalPlayback::GetPosition()
{
    return (double)player.PlaybackSession().Position().count() / winrt::impl::filetime_period::den;
}

double LocalPlayback::GetBufferedPosition()
{
    try
    {
        return player.PlaybackSession().BufferingProgress() * GetDuration();
    }
    catch (winrt::hresult_error const&)
    {
        return 0;
    }
}

double LocalPlayback::GetDuration()
{
    double duration = static_cast<double>(
        player.PlaybackSession().NaturalDuration().count() / winrt::impl::filetime_period::den);

    if (duration <= 0)
    {
        Track* track = GetCurrentTrack();
        duration = track != nullptr && track->Duration.count() > 0 ?
            static_cast<double>(track->Duration.count()) : 0;
    }

    return duration;
}

PlaybackState LocalPlayback::GetState()
{
    MediaPlaybackState state = player.PlaybackSession().PlaybackState();

    if (ended && (state == MediaPlaybackState::Paused || state == MediaPlaybackState::None))
        return PlaybackState::Stopped;
    else if (state == MediaPlaybackState::Opening || state == MediaPlaybackState::Buffering)
        return PlaybackState::Buffering;
    else if (state == MediaPlaybackState::None)
        return PlaybackState::None;
    else if (state == MediaPlaybackState::Paused)
        return PlaybackState::Paused;
    else if (state == MediaPlaybackState::Playing)
        return PlaybackState::Playing;

    return PlaybackState::None;
}

void LocalPlayback::OnStateChange(winrt::Windows::Media::Playback::MediaPlayer sender,
    const winrt::Windows::Foundation::IInspectable&)
{
    UpdateState(GetState());
}

void LocalPlayback::OnEnd(winrt::Windows::Media::Playback::MediaPlayer sender,
    const winrt::Windows::Foundation::IInspectable&)
{
    if (HasNext())
    {
        UpdateCurrentTrack(static_cast<size_t>(currentTrack) + 1, nullptr);
        Play();
    }
    else
    {
        manager.OnEnd(currentTrack, GetPosition());
    }
}

void LocalPlayback::OnError(winrt::Windows::Media::Playback::MediaPlayer sender,
    winrt::Windows::Media::Playback::MediaPlayerFailedEventArgs args)
{
    if (hasLoadCallback)
    {
        loadCallback.Reject(winrt::to_string(args.ErrorMessage()).c_str());
    }

    hasLoadCallback = false;

    VERBOSE_DEBUG(args.ErrorMessage().c_str());

    const char* code = "playback";

    if (args.Error() == MediaPlayerError::DecodingError || args.Error() == MediaPlayerError::SourceNotSupported)
        code = "playback-renderer";
    else if (args.Error() == MediaPlayerError::NetworkError)
        code = "playback-source";

    manager.OnError(code, winrt::to_string(args.ErrorMessage()));
}

void LocalPlayback::OnLoad(winrt::Windows::Media::Playback::MediaPlayer sender,
    const winrt::Windows::Foundation::IInspectable&)
{
    VERBOSE_DEBUG("OnLoad");

    if (startPos > 0)
    {
        player.PlaybackSession().Position(TimeSpan(static_cast<__int64>(startPos)));
        startPos = 0;
    }

    if (started)
    {
        Play();
    }

    if (hasLoadCallback)
    {
        loadCallback.Resolve(nullptr);
        new(&loadCallback) ReactPromise<JSValue>(IJSValueWriter(), MethodResultCallback(), MethodResultCallback());
        hasLoadCallback = false;
    }
}
