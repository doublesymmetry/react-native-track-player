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
    public static final String BUTTON_PLAY = "play";
    public static final String BUTTON_PAUSE = "pause";
    public static final String BUTTON_STOP = "stop";
    public static final String BUTTON_SKIP = "skip";
    public static final String BUTTON_SKIP_NEXT = "skipToNext";
    public static final String BUTTON_SKIP_PREVIOUS = "skipToPrevious";
    public static final String BUTTON_SEEK_TO = "seekTo";
    public static final String BUTTON_SET_RATING = "setRating";

    // Player Events
    public static final String PLAYER_STATE = "player-state";
    public static final String PLAYER_LOAD = "player-loaded";
    public static final String PLAYER_PLAY = "player-playing";
    public static final String PLAYER_PAUSE = "player-pause";
    public static final String PLAYER_STOP = "player-stopped";
    public static final String PLAYER_ENDED = "player-ended";
    public static final String PLAYER_ERROR = "player-error";

    // Remote Events
    public static final String REMOTE_ADDED = "device-added";
    public static final String REMOTE_REMOVED = "device-removed";
    public static final String REMOTE_CONNECTED = "device-connected";
    public static final String REMOTE_DISCONNECTED = "device-disconnected";

    public static void dispatchEvent(Context context, int player, String event, WritableMap data) {
        Intent i = new Intent(context, PlayerTask.class);

        if(event != null) i.putExtra(PlayerTask.EVENT_TYPE, event);
        if(data != null) i.putExtra(PlayerTask.EVENT_DATA, Arguments.toBundle(data));
        if(player != -1) i.putExtra(PlayerTask.EVENT_PLAYER, player);

        context.startService(i);
    }

}
