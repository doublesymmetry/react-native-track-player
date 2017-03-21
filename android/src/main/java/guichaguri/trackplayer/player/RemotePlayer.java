package guichaguri.trackplayer.player;

import android.content.Context;
import guichaguri.trackplayer.logic.MediaManager;

/**
 * @author Guilherme Chaguri
 */
public abstract class RemotePlayer extends Player {

    protected RemotePlayer(Context context, MediaManager manager) {
        super(context, manager);
    }

    public abstract float getVolume();

    public abstract boolean canChangeVolume();
}
