#include "pch.h"
#include "Logic/Track.h"
#include "Logic/Utils.h"

using namespace winrt::RNTrackPlayer;

Track::Track(const React::JSValueObject& data)
    : Url(Utils::GetValue(data, "url", "")),
      Type(Utils::GetValue(data, "type", TrackType::Default))
{
     SetMetadata(data);
}

void Track::SetMetadata(const React::JSValueObject& data)
{
    Duration = std::chrono::seconds(data["duration"].AsInt64());
    Title = data["title"].AsString();
    Artist = data["artist"].AsString();
    Album = data["album"].AsString();
    Artwork = data["artwork"].AsString();
}

React::JSValueObject Track::ToObject()
{
    React::JSValueObject obj;

    obj["url"] = Url;
    obj["type"] = Type;

    obj["duration"] = Duration.count();

    obj["title"] = Title;
    obj["artist"] = Artist;
    obj["album"] = Album;
    obj["artwork"] = Artwork;

    return obj;
}
