package guichaguri.trackplayer.metadata.components;

import android.content.Context;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import guichaguri.trackplayer.logic.Utils;

/**
 * @author Guilherme Chaguri
 */
public class ButtonListener extends MediaSessionCompat.Callback {

    private final Context context;

    public ButtonListener(Context context) {
        this.context = context;
    }

    @Override
    public void onPlay() {
        Utils.dispatchEvent(context, "play", null);
    }

    @Override
    public void onPause() {
        Utils.dispatchEvent(context, "pause", null);
    }

    @Override
    public void onStop() {
        Utils.dispatchEvent(context, "stop", null);
    }

    @Override
    public void onSkipToNext() {
        Utils.dispatchEvent(context, "skipNext", null);
    }

    @Override
    public void onSkipToPrevious() {
        Utils.dispatchEvent(context, "skipPrevious", null);
    }

    @Override
    public void onSeekTo(long pos) {
        WritableMap map = Arguments.createMap();
        Utils.setTime(map, "position", pos);
        Utils.dispatchEvent(context, "seekTo", map);
    }

    @Override
    public void onSetRating(RatingCompat rating) {
        WritableMap map = Arguments.createMap();
        Utils.setRating("rating", map, rating);
        Utils.dispatchEvent(context, "setRating", map);
    }

}
