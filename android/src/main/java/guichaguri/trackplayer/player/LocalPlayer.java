package guichaguri.trackplayer.player;

import android.content.Context;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.track.Track;
import guichaguri.trackplayer.player.components.PlayerView;

/**
 * Base player object for local players
 *
 * @author Guilherme Chaguri
 */
public abstract class LocalPlayer<T extends Track> extends Player<T> {

    protected LocalPlayer(Context context, MediaManager manager) {
        super(context, manager);
    }

    public abstract void bindView(PlayerView view);

}
