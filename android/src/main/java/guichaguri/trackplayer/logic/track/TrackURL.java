package guichaguri.trackplayer.logic.track;

import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.Utils;

/**
 * @author Guilherme Chaguri
 */
public class TrackURL {

    public final String url;
    public final boolean local;

    public TrackURL(ReadableMap data, String key) {
        local = Utils.isUrlLocal(data, key);
        url = Utils.getUrl(data, key, local);
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(o == this) return true;

        if(o instanceof TrackURL) {
            TrackURL data = (TrackURL)o;
            return local == data.local && url.equals(data.url);
        }

        return false;
    }
}
