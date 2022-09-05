#include "pch.h"
#include "RNTrackPlayer.h"
#include "Logic/Metadata.h"
#include "Logic/Utils.h"
#include "Players/Playback.h"

using namespace winrt::RNTrackPlayer;

void TrackPlayerModule::Init(ReactContext const& context) noexcept
{
    manager.reset(new MediaManager(context));
}

void TrackPlayerModule::GetConstantProvider(ReactConstantProvider& provider) noexcept
{
    // States
    provider.Add(L"STATE_NONE", static_cast<int>(PlaybackState::None));
    provider.Add(L"STATE_PLAYING", static_cast<int>(PlaybackState::Playing));
    provider.Add(L"STATE_PAUSED", static_cast<int>(PlaybackState::Paused));
    provider.Add(L"STATE_STOPPED", static_cast<int>(PlaybackState::Stopped));
    provider.Add(L"STATE_BUFFERING", static_cast<int>(PlaybackState::Buffering));

    // Capabilities
    provider.Add(L"CAPABILITY_PLAY", static_cast<int>(Capability::Play));
    provider.Add(L"CAPABILITY_PLAY_FROM_ID", static_cast<int>(Capability::Unsupported));
    provider.Add(L"CAPABILITY_PLAY_FROM_SEARCH", static_cast<int>(Capability::Unsupported));
    provider.Add(L"CAPABILITY_PAUSE", static_cast<int>(Capability::Pause));
    provider.Add(L"CAPABILITY_STOP", static_cast<int>(Capability::Stop));
    provider.Add(L"CAPABILITY_SEEK_TO", static_cast<int>(Capability::Seek));
    provider.Add(L"CAPABILITY_SKIP", static_cast<int>(Capability::Unsupported));
    provider.Add(L"CAPABILITY_SKIP_TO_NEXT", static_cast<int>(Capability::Next));
    provider.Add(L"CAPABILITY_SKIP_TO_PREVIOUS", static_cast<int>(Capability::Previous));
    provider.Add(L"CAPABILITY_SET_RATING", static_cast<int>(Capability::Unsupported));
    provider.Add(L"CAPABILITY_JUMP_FORWARD", static_cast<int>(Capability::JumpForward));
    provider.Add(L"CAPABILITY_JUMP_BACKWARD", static_cast<int>(Capability::JumpBackward));
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

void TrackPlayerModule::UpdateMetadataForTrack(const int index, JSValueObject metadata,
    ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    auto& queue = player->GetQueue();

    if (index < 0 || index > queue.size() - 1)
    {
        promise.Reject("The track index is out of bounds");
    }

    auto track = queue[index];
    track.SetMetadata(metadata);
    player->UpdateTrack(index, track);
    promise.Resolve(nullptr);
}

void TrackPlayerModule::RemoveUpcomingTracks(ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    player->RemoveUpcomingTracks();
    promise.Resolve(nullptr);
}

void TrackPlayerModule::Add(JSValueArray arr, int insertBeforeIndex,
    ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    std::vector<Track> tracks;
    tracks.reserve(arr.size());

    for (JSValue& obj : arr)
    {
        tracks.push_back(Track(obj.AsObject()));
    }

    player->Add(tracks, insertBeforeIndex, promise);
}

void TrackPlayerModule::Remove(JSValueArray arr, ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    std::vector<int> tracks;
    tracks.reserve(arr.size());

    for (const JSValue& id : arr)
    {
        tracks.push_back(id.AsInt32());
    }

    player->Remove(tracks, promise);
}

void TrackPlayerModule::Skip(const int trackId, double initialTime, ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    player->Skip(trackId, promise);

    if (initialTime > 0) {
        player->SeekTo(initialTime);
    }
}

void TrackPlayerModule::SkipToNext(double initialTime, ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    player->SkipToNext(promise);

    if (initialTime > 0) {
        player->SeekTo(initialTime);
    }
}

void TrackPlayerModule::SkipToPrevious(double initialTime, ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    player->SkipToPrevious(promise);

    if (initialTime > 0) {
        player->SeekTo(initialTime);
    }
}

void TrackPlayerModule::GetQueue(ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    auto queue = player->GetQueue();

    JSValueArray array;

    for (auto& track : queue)
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

    auto index = player->currentTrack;
    auto queue = player->GetQueue();

    if (index < 0 || index >= queue.size()) {
        promise.Resolve(index);
    } else {
        promise.Resolve(nullptr);
    }
}

void TrackPlayerModule::GetTrack(const int index, ReactPromise<JSValue> promise) noexcept
{
    auto player = manager ? manager->GetPlayer() : nullptr;
    if (Utils::CheckPlayback(player, promise))
        return;

    auto& queue = player->GetQueue();

    if (index >= 0 && index < queue.size()) {
        auto track = queue[index];
        promise.Resolve(track.ToObject());
    } else {
        promise.Resolve(nullptr);
    }
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
    if (player == nullptr)
    {
        promise.Resolve((int)PlaybackState::None);
        return;
    }

    promise.Resolve((int)player->GetState());
}
