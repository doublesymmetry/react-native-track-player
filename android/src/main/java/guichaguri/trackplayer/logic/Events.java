package guichaguri.trackplayer.logic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import guichaguri.trackplayer.logic.services.PlayerTask;

/**
 * @author Guilherme Chaguri
 */
public class Events {

    // Media Control Events
    public static final String BUTTON_PLAY = "play";
    public static final String BUTTON_PLAY_FROM_ID = "playFromId";
    public static final String BUTTON_PLAY_FROM_SEARCH = "playFromSearch";
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
    public static final String PLAYBACK_ENDED = "playback-ended";
    public static final String PLAYBACK_ERROR = "playback-error";

    // Remote Events
    public static final String CAST_STATE = "cast-state";
    public static final String CAST_CONNECTING = "cast-connecting";
    public static final String CAST_CONNECTED = "cast-connected";
    public static final String CAST_CONNECTION_FAILED = "cast-connection-failed";
    public static final String CAST_DISCONNECTING = "cast-disconnecting";
    public static final String CAST_DISCONNECTED = "cast-disconnected";

    public static void dispatchEvent(Context context, String event, Bundle data) {
        Intent i = new Intent(context, PlayerTask.class);

        if(event != null) i.putExtra(PlayerTask.EVENT_TYPE, event);
        if(data != null) i.putExtra(PlayerTask.EVENT_DATA, data);

        context.startService(i);
    }

}
