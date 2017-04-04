package guichaguri.trackplayer.logic.track;

import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.Utils;

/**
 * @author Guilherme Chaguri
 */
public class TrackCache {

    public final long maxSize;
    public final int maxFiles;

    public TrackCache(ReadableMap data, String key) {
        ReadableMap cache = Utils.getMap(data, key);

        if(cache != null) {
            maxSize = (long)(Utils.getDouble(cache, "maxSize", 0) * 1024);
            maxFiles = Utils.getInt(cache, "maxFiles", 0);
        } else {
            maxSize = 0;
            maxFiles = 0;
        }
    }

}
