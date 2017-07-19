package guichaguri.trackplayer.logic.track;

import android.os.Bundle;

/**
 * @author Guilherme Chaguri
 */
public enum TrackType {

    DEFAULT("default"),
    DASH("dash"),
    HLS("hls"),
    SMOOTH_STREAMING("smoothstreaming");

    public final String name;

    TrackType(String name) {
        this.name = name;
    }

    public static TrackType fromString(String name) {
        for(TrackType type : values()) {
            if(type.name.equalsIgnoreCase(name)) return type;
        }
        return TrackType.DEFAULT;
    }

    public static TrackType fromBundle(Bundle data, String key) {
        return fromString(data.getString(key, "default").toLowerCase());
    }

}
