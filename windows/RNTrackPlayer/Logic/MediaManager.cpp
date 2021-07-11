#include "pch.h"
#include "Logic/MediaManager.h"
#include "Logic/Events.h"
#include "Logic/Track.h"

using namespace winrt::RNTrackPlayer;
using namespace winrt::Windows::UI::Xaml;

MediaManager::MediaManager(React::ReactContext const& context)
    : player(nullptr),
      metadata(*this)
{
    this->context = context;
}

void MediaManager::SendEvent(const std::string& eventName, const JSValueObject& data)
{
    std::wstring _eventName(eventName.begin(), eventName.end());
    context.EmitJSEvent(L"RCTDeviceEventEmitter", _eventName, data);
}

void MediaManager::SwitchPlayback(Playback* pb)
{
    if (player != nullptr)
    {
        player->Stop();
        delete player;
    }

    player = pb;
    if (pb)
    {
        metadata.SetTransportControls(pb->GetTransportControls());
    }
}

LocalPlayback* MediaManager::CreateLocalPlayback(React::JSValueObject& options)
{
    return new LocalPlayback(*this, options);
}

void MediaManager::UpdateOptions(React::JSValueObject& options)
{
    metadata.UpdateOptions(options);
}

Playback* MediaManager::GetPlayer() const
{
    return const_cast<Playback*>(player);
}

Metadata* MediaManager::GetMetadata() const
{
    return const_cast<Metadata*>(&metadata);
}

void MediaManager::OnEnd(int previous, double prevPos)
{
    VERBOSE_DEBUG("OnEnd");

    JSValueObject obj;
    if (previous != -1)
    {
        obj["track"] = previous;
    }
    obj["position"] = prevPos;
    SendEvent(Events::PlaybackQueueEnded, obj);
}

void MediaManager::OnStateChange(PlaybackState state)
{
    VERBOSE_DEBUG("OnStateChange");

    JSValueObject obj;
    obj["state"] = (int)state;
    SendEvent(Events::PlaybackState, obj);
}

void MediaManager::OnTrackUpdate(int previousIndex, double prevPos, int nextIndex, Track* next)
{
    VERBOSE_DEBUG("OnTrackUpdate");

    if (next)
    {
        metadata.UpdateMetadata(*next);
    }

    JSValueObject obj;
    if (previousIndex != -1)
    {
        obj["track"] = previousIndex;
    }
    obj["position"] = prevPos;
    if (nextIndex != -1)
    {
        obj["nextTrack"] = nextIndex;
    }

    SendEvent(Events::PlaybackTrackChanged, obj);
}

void MediaManager::OnError(const std::string& code, const std::string& error)
{
    VERBOSE_DEBUG(error);

    JSValueObject obj;
    obj["code"] = code;
    obj["message"] = error;
    SendEvent(Events::PlaybackError, obj);
}

