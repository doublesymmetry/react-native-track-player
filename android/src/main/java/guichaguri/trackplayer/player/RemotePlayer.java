package guichaguri.trackplayer.player;

import android.content.Context;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.track.Track;

/**
 * Base player object for remote players
 *
 * @author Guilherme Chaguri
 */
public abstract class RemotePlayer<T extends Track> extends Player<T> {

    protected RemotePlayer(Context context, MediaManager manager) {
        super(context, manager);
    }

    public abstract float getVolume();

    public abstract boolean canChangeVolume();
}
