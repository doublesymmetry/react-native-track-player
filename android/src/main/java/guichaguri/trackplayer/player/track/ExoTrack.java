package guichaguri.trackplayer.player.track;

import android.content.Context;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.exoplayer2.util.Util;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.track.Track;
import guichaguri.trackplayer.logic.track.TrackType;

/**
 * @author Guilherme Chaguri
 */
public class ExoTrack extends Track {

    public final TrackType type;
    public final String userAgent;

    public ExoTrack(Context context, MediaManager manager, ReadableMap data) {
        super(manager, data);

        type = TrackType.fromMap(data, "type");
        userAgent = Utils.getString(data, "useragent", Util.getUserAgent(context, "react-native-track-player"));
    }

}
