#include "pch.h"
#include "Logic/Utils.h"
#include "Players/Playback.h"

using namespace winrt::RNTrackPlayer;

bool Utils::IsPlaying(PlaybackState state)
{
    return state == PlaybackState::Playing || state == PlaybackState::Buffering;
}

bool Utils::IsPaused(PlaybackState state)
{
    return state == PlaybackState::Paused;
}

std::string Utils::GetValue(const JSValueObject& obj, const std::string& key, const std::string& def)
{
    const JSValue& val = obj[key];
    return val == JSValue::Null ? def : val.AsString();
}

winrt::Windows::Foundation::Uri Utils::GetUri(const React::JSValueObject& obj,
    const std::string& key, winrt::Windows::Foundation::Uri def)
{
    const JSValue& val = obj[key];
    if (val == JSValue::Null)
        return def;

    if (val.Type() == JSValueType::Object)
    {
        const JSValue& uri = val.AsObject()["uri"];
        if (uri == JSValue::Null)
            return def;

        return Uri(winrt::to_hstring(uri.AsString()));
    }
    else if (val.Type() == JSValueType::String)
    {
        return Uri(winrt::to_hstring(val.AsString()));
    }

    return def;
}

bool Utils::CheckPlayback(Playback* pb, ReactPromise<JSValue>& promise)
{
    if (pb == nullptr)
    {
        promise.Reject("The playback is not initialized");
        return true;
    }

    return false;
}

bool Utils::ContainsInt(const React::JSValueArray& array, int val)
{
    for (int i = 0; i < array.size(); i++)
    {
        auto& token = array[i];
        if (token.Type() == JSValueType::Int64 && token.AsInt32() == val)
        {
            return true;
        }
    }

    return false;
}

