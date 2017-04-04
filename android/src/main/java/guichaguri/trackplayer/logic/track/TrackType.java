package guichaguri.trackplayer.logic.track;

import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.Utils;

/**
 * @author Guilherme Chaguri
 */
public enum TrackType {

    DEFAULT("default"),
    DASH("dash"),
    HLS("hls"),
    SMOOTH_STREAMING("smoothstreaming");

    private final String name;

    TrackType(String name) {
        this.name = name;
    }

    public static TrackType fromString(String name) {
        for(TrackType type : values()) {
            if(type.name.equalsIgnoreCase(name)) return type;
        }
        return TrackType.DEFAULT;
    }

    public static TrackType fromMap(ReadableMap data, String key) {
        return fromString(Utils.getString(data, key, "default").toLowerCase());
    }

}
