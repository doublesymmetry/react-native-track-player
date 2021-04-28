#include "pch.h"
#include "Logic/MediaManager.h"
#include "Logic/Utils.h"
#include "Players/Playback.h"

using namespace winrt::RNTrackPlayer;

Playback::Playback(MediaManager& manager)
    : manager(manager)
{
}

Playback::~Playback()
{
}

void Playback::UpdateState(PlaybackState state)
{
    if (prevState == state)
    {
        return;
    }

    manager.OnStateChange(state);
    prevState = state;
}

void Playback::UpdateCurrentTrack(size_t index, React::ReactPromise<JSValue>* promise)
{
    if (queue.size() == 0)
    {
        Reset();

        if (promise)
            promise->Reject("The queue is empty");

        return;
    }
    else if (index < 0)
    {
        index = 0;
    }
    else if (index >= queue.size())
    {
        index = queue.size() - 1;
    }

    Track* previous = GetCurrentTrack();
    double position = GetPosition();
    PlaybackState oldState = GetState();

    VERBOSE_DEBUG("Updating current track...");

    Track& track = queue[index];
    currentTrack = static_cast<int>(index);

    Load(track, promise);

    if (Utils::IsPlaying(oldState))
        Play();
    else if (Utils::IsPaused(oldState))
        Pause();

    manager.OnTrackUpdate(previous, position, &track, true);
}

Track* Playback::GetCurrentTrack()
{
    return currentTrack >= 0 && currentTrack < queue.size() ? &queue[currentTrack] : nullptr;
}

Track* Playback::GetTrack(const std::string& id)
{
    auto it = std::find_if(queue.begin(), queue.end(), [&](const Track& t) { return t.Id == id; });
    return it == queue.end() ? nullptr : &(*it);
}

std::vector<Track>& Playback::GetQueue()
{
    return queue;
}

void Playback::Add(std::vector<Track>& tracks, std::string& insertBeforeId,
    React::ReactPromise<JSValue>& promise)
{
    if (insertBeforeId.empty())
    {
        bool empty = queue.size() == 0;
        for (auto& track : tracks)
            queue.push_back(track);

        // Tracks were added, we'll update the current track accordingly
        if (empty)
            UpdateCurrentTrack(0, nullptr);
    }
    else
    {
        auto it = std::find_if(queue.begin(), queue.end(), [&](const Track& t) { return t.Id == insertBeforeId; });
        auto index = it == queue.end() ? -1 : std::distance(queue.begin(), it);
        if (index == -1) index = queue.size();

        queue.insert(queue.begin() + index, tracks.begin(), tracks.end());

        if (currentTrack >= index)
            currentTrack += static_cast<int>(tracks.size());
    }

    promise.Resolve(nullptr);
}

void Playback::Remove(std::vector<std::string>& ids, React::ReactPromise<JSValue>& promise)
{
    int currTrack = currentTrack;

    for (std::string& id : ids)
    {
        auto it = std::find_if(queue.begin(), queue.end(), [&](const Track& t) { return t.Id == id; });
        auto index = std::distance(queue.begin(), it);

        queue.erase(queue.begin() + index);

        if (index == currTrack)
        {
            currTrack += 1;
        }
    }

    if (currTrack != currentTrack)
    {
        UpdateCurrentTrack(currTrack, nullptr);
    }

    promise.Resolve(nullptr);
}

void Playback::UpdateTrack(size_t index, Track& track)
{
    queue[index] = track;

    if (index == currentTrack)
    {
        manager.GetMetadata()->UpdateMetadata(track);
    }
}

void Playback::Reset()
{
    Track* prev = GetCurrentTrack();
    double pos = GetPosition();

    Stop();

    currentTrack = -1;
    queue.clear();

    manager.OnTrackUpdate(prev, pos, nullptr, true);
}

void Playback::RemoveUpcomingTracks()
{
    queue.erase(queue.begin() + currentTrack, queue.end());
}

void Playback::Skip(std::string id, React::ReactPromise<JSValue>& promise)
{
    auto it = std::find_if(queue.begin(), queue.end(), [&](const Track& t) { return t.Id == id; });
    auto index = it == queue.end() ? -1 : std::distance(queue.begin(), it);

    if (index >= 0)
        UpdateCurrentTrack(index, &promise);
    else
        promise.Reject("Given track ID was not found in queue");
}

bool Playback::HasNext()
{
    return currentTrack < queue.size() - 1;
}

void Playback::SkipToNext(React::ReactPromise<JSValue>& promise)
{
    if (HasNext())
        UpdateCurrentTrack(static_cast<size_t>(currentTrack) + 1, &promise);
    else
        promise.Reject("There is no tracks left to play");
}

void Playback::SkipToPrevious(React::ReactPromise<JSValue>& promise)
{
    if (currentTrack > 0)
        UpdateCurrentTrack(static_cast<size_t>(currentTrack) - 1, &promise);
    else
        promise.Reject("There is no previous tracks");
}
