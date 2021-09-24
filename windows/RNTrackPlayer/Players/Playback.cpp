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

    manager.OnTrackUpdate(currentTrack, position, index, &track);
}

Track* Playback::GetCurrentTrack()
{
    return currentTrack >= 0 && currentTrack < queue.size() ? &queue[currentTrack] : nullptr;
}

std::vector<Track>& Playback::GetQueue()
{
    return queue;
}

void Playback::Add(std::vector<Track>& tracks, int insertBeforeIndex,
    React::ReactPromise<JSValue>& promise)
{
    if (insertBeforeIndex > 0 && insertBeforeIndex > queue.size())
    {
        promise.Reject("The track index is out of bounds");
    }
    else if (insertBeforeIndex == -1)
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
        queue.insert(queue.begin() + insertBeforeIndex, tracks.begin(), tracks.end());

        if (currentTrack >= insertBeforeIndex)
            currentTrack += static_cast<int>(tracks.size());
    }

    promise.Resolve(nullptr);
}

void Playback::Remove(std::vector<int> indexes, React::ReactPromise<JSValue>& promise)
{
    int currTrack = currentTrack;

    for (int index : indexes)
    {
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
    double pos = GetPosition();

    Stop();

    manager.OnTrackUpdate(currentTrack, pos, -1, nullptr);

    currentTrack = -1;
    queue.clear();
}

void Playback::RemoveUpcomingTracks()
{
    queue.erase(queue.begin() + currentTrack, queue.end());
}

void Playback::Skip(int index, React::ReactPromise<JSValue>& promise)
{
    if (index < 0 || index >= queue.size()) {
        promise.Reject("The track index is out of bounds");
    }
    else
        UpdateCurrentTrack(index, &promise);
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
        promise.Reject("There are no tracks left to play");
}

void Playback::SkipToPrevious(React::ReactPromise<JSValue>& promise)
{
    if (currentTrack > 0)
        UpdateCurrentTrack(static_cast<size_t>(currentTrack) - 1, &promise);
    else
        promise.Reject("There are no previous tracks");
}
