#include "pch.h"
#include "Logic/Events.h"
#include "Logic/MediaManager.h"
#include "Logic/Metadata.h"
#include "Logic/Track.h"
#include "Logic/Utils.h"

using namespace winrt::RNTrackPlayer;
using namespace winrt::Windows::Media;
using namespace winrt::Windows::Storage::Streams;

Metadata::Metadata(MediaManager& manager)
    : controls(nullptr),
      manager(manager)
{
}

Metadata::~Metadata()
{
    if (controls)
    {
        controls.IsEnabled(false);
        controls.PlaybackPositionChangeRequested(onSeekToRevoker);
        onSeekToRevoker = winrt::event_token();
        controls.ButtonPressed(onButtonPressedRevoker);
        onButtonPressedRevoker = winrt::event_token();
        controls = nullptr;
    }
}

void Metadata::SetTransportControls(winrt::Windows::Media::SystemMediaTransportControls transportControls)
{
    if (controls != nullptr)
    {
        controls.IsEnabled(false);

        controls.PlaybackPositionChangeRequested(onSeekToRevoker);
        onSeekToRevoker = winrt::event_token();

        controls.ButtonPressed(onButtonPressedRevoker);
        onButtonPressedRevoker = winrt::event_token();
    }

    controls = transportControls;

    if (controls != nullptr)
    {
        controls.IsEnabled(true);

        onSeekToRevoker = controls.PlaybackPositionChangeRequested({ this, &Metadata::OnSeekTo });
        onButtonPressedRevoker = controls.ButtonPressed({ this, &Metadata::OnButtonPressed });

        UpdateCapabilities();
    }
}

void Metadata::UpdateCapabilities()
{
    controls.IsPlayEnabled(play);
    controls.IsPauseEnabled(pause);
    controls.IsStopEnabled(stop);
    controls.IsPreviousEnabled(previous);
    controls.IsNextEnabled(next);
    controls.IsFastForwardEnabled(jumpForward);
    controls.IsRewindEnabled(jumpBackward);

    // Unsupported for now
    controls.IsChannelDownEnabled(false);
    controls.IsChannelUpEnabled(false);
    controls.IsRecordEnabled(false);
}

void Metadata::UpdateOptions(React::JSValueObject& data)
{
    VERBOSE_DEBUG("Updating options...");

    if (data.find("jumpInterval") != data.end())
    {
        jumpInterval = data["jumpInterval"].AsDouble();
    }

    if (data.find("capabilities") != data.end())
    {
        auto& capabilities = data["capabilities"].AsArray();

        play = Utils::ContainsInt(capabilities, static_cast<int>(Capability::Play));
        pause = Utils::ContainsInt(capabilities, static_cast<int>(Capability::Pause));
        stop = Utils::ContainsInt(capabilities, static_cast<int>(Capability::Stop));
        previous = Utils::ContainsInt(capabilities, static_cast<int>(Capability::Previous));
        next = Utils::ContainsInt(capabilities, static_cast<int>(Capability::Next));
        jumpForward = Utils::ContainsInt(capabilities, static_cast<int>(Capability::JumpForward));
        jumpBackward = Utils::ContainsInt(capabilities, static_cast<int>(Capability::JumpBackward));
        seek = Utils::ContainsInt(capabilities, static_cast<int>(Capability::Seek));

        if (controls != nullptr)
        {
            UpdateCapabilities();
        }
    }
}

void Metadata::UpdateMetadata(Track& track)
{
    auto display = controls.DisplayUpdater();

    try
    {
        Uri thumbnailUri(winrt::to_hstring(track.Artwork));
        display.Thumbnail(RandomAccessStreamReference::CreateFromUri(thumbnailUri));
    }
    catch (winrt::hresult_error const&)
    {
    }

    display.Type(MediaPlaybackType::Music);

    auto properties = display.MusicProperties();
    if (properties)
    {
        properties.Title(winrt::to_hstring(track.Title));
        properties.Artist(winrt::to_hstring(track.Artist));
        properties.AlbumTitle(winrt::to_hstring(track.Album));
    }
}

void Metadata::OnSeekTo(winrt::Windows::Media::SystemMediaTransportControls sender,
    winrt::Windows::Media::PlaybackPositionChangeRequestedEventArgs args)
{
    if (!seek)
    {
        return;
    }

    JSValueObject obj;
    obj["position"] = args.RequestedPlaybackPosition().count();

    manager.SendEvent(Events::ButtonSeekTo, obj);
}

void Metadata::OnButtonPressed(winrt::Windows::Media::SystemMediaTransportControls sender,
    winrt::Windows::Media::SystemMediaTransportControlsButtonPressedEventArgs args)
{
    std::string eventType;
    JSValueObject data;

    switch (args.Button())
    {
    case SystemMediaTransportControlsButton::Play:
        eventType = Events::ButtonPlay;
        break;
    case SystemMediaTransportControlsButton::Pause:
        eventType = Events::ButtonPause;
        break;
    case SystemMediaTransportControlsButton::Stop:
        eventType = Events::ButtonStop;
        break;
    case SystemMediaTransportControlsButton::Previous:
        eventType = Events::ButtonSkipPrevious;
        break;
    case SystemMediaTransportControlsButton::Next:
        eventType = Events::ButtonSkipNext;
        break;
    case SystemMediaTransportControlsButton::FastForward:
        eventType = Events::ButtonJumpForward;
        data["interval"] = jumpInterval;
        break;
    case SystemMediaTransportControlsButton::Rewind:
        eventType = Events::ButtonJumpBackward;
        data["interval"] = jumpInterval;
        break;
    default:
        return;
    }

    manager.SendEvent(eventType, data);
}
