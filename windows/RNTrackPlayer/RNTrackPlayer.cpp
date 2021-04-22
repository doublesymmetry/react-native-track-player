#include "pch.h"
#include "RNTrackPlayer.h"
#include "Logic/Metadata.h"
#include "Logic/Utils.h"
#include "Players/Playback.h"

using namespace winrt::RNTrackPlayer;

void TrackPlayerModule::Init(ReactContext const& context) noexcept
{
    manager = new MediaManager(context);
}

void TrackPlayerModule::GetConstantProvider(ReactConstantProvider& provider) noexcept
{
    // States
    provider.Add(L"STATE_NONE", (int)PlaybackState::None);
    provider.Add(L"STATE_PLAYING", (int)PlaybackState::Playing);
    provider.Add(L"STATE_PAUSED", (int)PlaybackState::Paused);
    provider.Add(L"STATE_STOPPED", (int)PlaybackState::Stopped);
    provider.Add(L"STATE_BUFFERING", (int)PlaybackState::Buffering);

    // Capabilities
    provider.Add(L"CAPABILITY_PLAY", (int)Capability::Play);
    provider.Add(L"CAPABILITY_PLAY_FROM_ID", (int)Capability::Unsupported);
    provider.Add(L"CAPABILITY_PLAY_FROM_SEARCH", (int)Capability::Unsupported);
    provider.Add(L"CAPABILITY_PAUSE", (int)Capability::Pause);
    provider.Add(L"CAPABILITY_STOP", (int)Capability::Stop);
    provider.Add(L"CAPABILITY_SEEK_TO", (int)Capability::Seek);
    provider.Add(L"CAPABILITY_SKIP", (int)Capability::Unsupported);
    provider.Add(L"CAPABILITY_SKIP_TO_NEXT", (int)Capability::Next);
    provider.Add(L"CAPABILITY_SKIP_TO_PREVIOUS", (int)Capability::Previous);
    provider.Add(L"CAPABILITY_SET_RATING", (int)Capability::Unsupported);
    provider.Add(L"CAPABILITY_JUMP_FORWARD", (int)Capability::JumpForward);
    provider.Add(L"CAPABILITY_JUMP_BACKWARD", (int)Capability::JumpBackward);
}

void TrackPlayerModule::SetupPlayer(JSValueObject options, ReactPromise<JSValue> promise) noexcept
{
    if (manager->GetPlayer() != nullptr)
    {
        promise.Resolve(nullptr);
        return;
    }

    manager->SwitchPlayback(manager->CreateLocalPlayback(options));
    promise.Resolve(nullptr);
}

void TrackPlayerModule::Destroy() noexcept
{
    if (!manager)
        return;
    
    manager->SwitchPlayback(nullptr);
}

void TrackPlayerModule::UpdateOptions(JSValueObject options, ReactPromise<JSValue> promise) noexcept
{
    manager->UpdateOptions(options);
    promise.Resolve(nullptr);
}

void TrackPlayerModule::Play(ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    player->Play();
    promise.Resolve(nullptr);
}

void TrackPlayerModule::Pause(ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    player->Pause();
    promise.Resolve(nullptr);
}

void TrackPlayerModule::Stop(ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    player->Stop();
    promise.Resolve(nullptr);
}

void TrackPlayerModule::Reset(ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    player->Reset();
    promise.Resolve(nullptr);
}

void TrackPlayerModule::UpdateMetadataForTrack(std::string id, JSValueObject metadata,
    ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    auto& queue = player->GetQueue();

    auto it = std::find_if(queue.begin(), queue.end(), [&](const Track& t) { return t.Id == id; });
    auto index = it == queue.end() ? -1 : std::distance(queue.begin(), it);

    if (index == -1)
    {
        promise.Reject("Track not found");
    }
    else
    {
        auto track = queue[index];
        track.SetMetadata(metadata);
        player->UpdateTrack(index, track);
        promise.Resolve(nullptr);
    }
}

void TrackPlayerModule::RemoveUpcomingTracks(ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    player->RemoveUpcomingTracks();
    promise.Resolve(nullptr);
}

void TrackPlayerModule::Add(JSValueArray arr, std::string insertBeforeId,
    ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    std::vector<Track> tracks;
    tracks.reserve(arr.size());

    for(JSValue& obj : arr)
    {
        tracks.push_back(Track(obj.AsObject()));
    }

    player->Add(tracks, insertBeforeId, promise);
}

void TrackPlayerModule::Remove(JSValueArray arr, ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    std::vector<std::string> tracks;
    tracks.reserve(arr.size());

    for(const JSValue& id : arr)
    {
        tracks.push_back(id.AsString());
    }

    player->Remove(tracks, promise);
}

void TrackPlayerModule::Skip(std::string track, ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    player->Skip(track, promise);
}

void TrackPlayerModule::SkipToNext(ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    player->SkipToNext(promise);
}

void TrackPlayerModule::SkipToPrevious(ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    player->SkipToPrevious(promise);
}

void TrackPlayerModule::GetQueue(ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    auto queue = player->GetQueue();

    JSValueArray array;

    for(auto& track : queue)
    {
        array.push_back(track.ToObject());
    }

    promise.Resolve(std::move(array));
}

void TrackPlayerModule::GetCurrentTrack(ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    auto track = player->GetCurrentTrack();
    promise.Resolve(track ? track->Id.c_str() : JSValue());
}

void TrackPlayerModule::GetTrack(std::string id, ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    auto track = player->GetTrack(id);
    promise.Resolve(track ? track->ToObject() : JSValueObject());
}

void TrackPlayerModule::GetVolume(ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    promise.Resolve(player->GetVolume());
}

void TrackPlayerModule::SetVolume(double volume, ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    player->SetVolume(volume);
    promise.Resolve(nullptr);
}

void TrackPlayerModule::GetRate(ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    promise.Resolve(player->GetRate());
}

void TrackPlayerModule::SetRate(double rate, ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    player->SetRate(rate);
    promise.Resolve(nullptr);
}

void TrackPlayerModule::SeekTo(double seconds, ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    player->SeekTo(seconds);
    promise.Resolve(nullptr);
}

void TrackPlayerModule::GetPosition(ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    promise.Resolve(player->GetPosition());
}

void TrackPlayerModule::GetBufferedPosition(ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    promise.Resolve(player->GetBufferedPosition());
}

void TrackPlayerModule::GetDuration(ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    promise.Resolve(player->GetDuration());
}

void TrackPlayerModule::GetState(ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    promise.Resolve((int)player->GetState());
}
