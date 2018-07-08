package com.guichaguri.trackplayer.service.models;

/**
 * @author Guichaguri
 */
public enum TrackType {

    /**
     * The default media type. Should be used for streams over HTTP or files
     */
    DEFAULT,

    /**
     * The DASH media type for adaptive streams. Should be used with DASH manifests
     */
    DASH,

    /**
     * The HLS media type for adaptive streams. Should be used with HLS playlists
     */
    HLS,

    /**
     * The SmoothStreaming media type for adaptive streams. Should be used with SmoothStreaming manifests
     */
    SMOOTH_STREAMING

}
