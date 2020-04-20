package com.guichaguri.trackplayer.module;

/**
 * @author Guichaguri
 */
public final class MusicEvents {

    // Media Control Events
    public static final String BUTTON_PLAY = "remote-play";
    public static final String BUTTON_PLAY_FROM_ID = "remote-play-id";
    public static final String BUTTON_PLAY_FROM_SEARCH = "remote-play-search";
    public static final String BUTTON_PAUSE = "remote-pause";
    public static final String BUTTON_STOP = "remote-stop";
    public static final String BUTTON_SKIP = "remote-skip";
    public static final String BUTTON_SKIP_NEXT = "remote-next";
    public static final String BUTTON_SKIP_PREVIOUS = "remote-previous";
    public static final String BUTTON_SEEK_TO = "remote-seek";
    public static final String BUTTON_SET_RATING = "remote-set-rating";
    public static final String BUTTON_JUMP_FORWARD = "remote-jump-forward";
    public static final String BUTTON_JUMP_BACKWARD = "remote-jump-backward";
    public static final String BUTTON_DUCK = "remote-duck";

    // Playback Events
    public static final String PLAYBACK_STATE = "playback-state";
    public static final String PLAYBACK_TRACK_CHANGED = "playback-track-changed";
    public static final String PLAYBACK_QUEUE_ENDED = "playback-queue-ended";
    public static final String PLAYBACK_METADATA = "playback-metadata-received";
    public static final String PLAYBACK_ERROR = "playback-error";

}
