package guichaguri.trackplayer.logic.components;

import android.os.Binder;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.player.LocalPlayer;
import guichaguri.trackplayer.player.Player;
import guichaguri.trackplayer.player.components.PlayerView;

/**
 * A wrapper of {@link MediaManager} to be used as a lightweight {@link android.os.IBinder} for binding views to players
 * @author Guilherme Chaguri
 */
public class VideoWrapper extends Binder {

    private final MediaManager manager;

    public VideoWrapper(MediaManager manager) {
        this.manager = manager;
    }

    public void setView(int id, PlayerView view) {
        Player player = manager.getPlayer(id);
        if(player instanceof LocalPlayer) {
            ((LocalPlayer)player).bindView(view);
        }
    }

}
