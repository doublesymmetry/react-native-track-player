namespace TrackPlayer.Logic
{

    public static class Events
    {
        public const string ButtonPlay = "remote-play";
        public const string ButtonPause = "remote-pause";
        public const string ButtonStop = "remote-stop";
        public const string ButtonSkipNext = "remote-next";
        public const string ButtonSkipPrevious = "remote-previous";
        public const string ButtonSeekTo = "remote-seek";
        public const string ButtonJumpForward = "remote-jump-forward";
        public const string ButtonJumpBackward = "remote-jump-backward";

        public const string PlaybackState = "playback-state";
        public const string PlaybackTrackChanged = "playback-track-changed";
        public const string PlaybackQueueEnded = "playback-queue-ended";
        public const string PlaybackError = "playback-error";
    }

}
