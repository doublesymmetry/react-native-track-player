package guichaguri.trackplayer.metadata.components;

import android.content.Context;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;

/**
 * @author Guilherme Chaguri
 */
public class ButtonListener extends MediaSessionCompat.Callback {

    private final Context context;
    private final MediaManager manager;

    public ButtonListener(Context context, MediaManager manager) {
        this.context = context;
        this.manager = manager;
    }

    private void dispatch(String event, WritableMap data) {
        Utils.dispatchEvent(context, manager.getPlayerId(manager.getMainPlayer()), event, data);
    }

    @Override
    public void onPlay() {
        dispatch("play", null);
    }

    @Override
    public void onPause() {
        dispatch("pause", null);
    }

    @Override
    public void onStop() {
        dispatch("stop", null);
    }

    @Override
    public void onSkipToNext() {
        dispatch("skipNext", null);
    }

    @Override
    public void onSkipToPrevious() {
        dispatch("skipPrevious", null);
    }

    @Override
    public void onSeekTo(long pos) {
        WritableMap map = Arguments.createMap();
        Utils.setTime(map, "position", pos);
        dispatch("seekTo", map);
    }

    @Override
    public void onSetRating(RatingCompat rating) {
        WritableMap map = Arguments.createMap();
        Utils.setRating("rating", map, rating);
        dispatch("setRating", map);
    }

}
