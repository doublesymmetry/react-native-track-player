package guichaguri.trackplayer.logic.components;

import android.os.Binder;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.player.view.PlayerView;

/**
 * @author Guilherme Chaguri
 */
public class VideoWrapper extends Binder {

    private final MediaManager manager;

    public VideoWrapper(MediaManager manager) {
        this.manager = manager;
    }

    public void setView(int id, PlayerView view) {
        manager.getPlayer(id).bindView(view);
    }

}
