package guichaguri.trackplayer.player;

import android.content.Context;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.track.Track;

/**
 * @author Guilherme Chaguri
 */
public abstract class RemotePlayer<T extends Track> extends Player<T> {

    protected RemotePlayer(Context context, MediaManager manager) {
        super(context, manager);
    }

    public abstract float getVolume() throws Exception;

    public abstract boolean canChangeVolume();
}
