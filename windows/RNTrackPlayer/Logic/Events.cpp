#include "pch.h"
#include "Logic/Events.h"

using namespace winrt::RNTrackPlayer;

const char* Events::ButtonPlay = "remote-play";
const char* Events::ButtonPause = "remote-pause";
const char* Events::ButtonStop = "remote-stop";
const char* Events::ButtonSkipNext = "remote-next";
const char* Events::ButtonSkipPrevious = "remote-previous";
const char* Events::ButtonSeekTo = "remote-seek";
const char* Events::ButtonJumpForward = "remote-jump-forward";
const char* Events::ButtonJumpBackward = "remote-jump-backward";

const char* Events::PlaybackState = "playback-state";
const char* Events::PlaybackTrackChanged = "playback-track-changed";
const char* Events::PlaybackQueueEnded = "playback-queue-ended";
const char* Events::PlaybackError = "playback-error";
