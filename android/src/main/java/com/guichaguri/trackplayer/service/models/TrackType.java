package com.guichaguri.trackplayer.service.models;

/**
 * @author Guichaguri
 */
public enum TrackType {

    /**
     * The default media type. Should be used for streams over HTTP or files
     */
    DEFAULT("default"),

    /**
     * The DASH media type for adaptive streams. Should be used with DASH manifests
     */
    DASH("dash"),

    /**
     * The HLS media type for adaptive streams. Should be used with HLS playlists
     */
    HLS("hls"),

    /**
     * The SmoothStreaming media type for adaptive streams. Should be used with SmoothStreaming manifests
     */
    SMOOTH_STREAMING("smoothstreaming"),

    /**
     * The ICY_METADATA media type for adaptive streams. Should be used with icymetadata manifests
     */
    ICY_METADATA("icymetadata");

    public final String name;

    TrackType(String name) {
        this.name = name;
    }

}
