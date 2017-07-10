package guichaguri.trackplayer.logic;

import android.content.Context;
import android.content.Intent;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import guichaguri.trackplayer.logic.workers.PlayerTask;

/**
 * @author Guilherme Chaguri
 */
public class Events {

    // Media Control Events
    public static final String BUTTON_LOAD = "load";
    public static final String BUTTON_PLAY = "play";
    public static final String BUTTON_PAUSE = "pause";
    public static final String BUTTON_STOP = "stop";
    public static final String BUTTON_SKIP = "skip";
    public static final String BUTTON_SKIP_NEXT = "skipToNext";
    public static final String BUTTON_SKIP_PREVIOUS = "skipToPrevious";
    public static final String BUTTON_SEEK_TO = "seekTo";
    public static final String BUTTON_SET_RATING = "setRating";
    public static final String BUTTON_DUCK = "duck";

    // Playback Events
    public static final String PLAYBACK_STATE = "playback-state";
    public static final String PLAYBACK_LOAD = "playback-loaded";
    public static final String PLAYBACK_PLAY = "playback-started";
    public static final String PLAYBACK_PAUSE = "playback-paused";
    public static final String PLAYBACK_STOP = "playback-stopped";
    public static final String PLAYBACK_ENDED = "playback-ended";
    public static final String PLAYBACK_ERROR = "playback-error";

    // Remote Events
    public static final String REMOTE_ADDED = "device-added";
    public static final String REMOTE_REMOVED = "device-removed";
    public static final String REMOTE_CONNECTED = "device-connected";
    public static final String REMOTE_DISCONNECTED = "device-disconnected";

    public static void dispatchEvent(Context context, String event, WritableMap data) {
        Intent i = new Intent(context, PlayerTask.class);

        if(event != null) i.putExtra(PlayerTask.EVENT_TYPE, event);
        if(data != null) i.putExtra(PlayerTask.EVENT_DATA, Arguments.toBundle(data));

        context.startService(i);
    }

}
