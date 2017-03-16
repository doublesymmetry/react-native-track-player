package guichaguri.trackplayer.player;

import android.content.Context;

/**
 * @author Guilherme Chaguri
 */
public abstract class RemotePlayer extends Player {

    protected RemotePlayer(Context context) {
        super(context);
    }

    public abstract float getVolume();

    public abstract boolean canChangeVolume();
}
