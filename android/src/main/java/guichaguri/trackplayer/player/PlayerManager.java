package guichaguri.trackplayer.player;

import android.content.Context;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.player.components.FocusManager;

/**
 * @author Guilherme Chaguri
 */
public class PlayerManager {

    private final Context context;
    private final FocusManager focus;

    private Player[] players = new Player[0];//TODO

    public PlayerManager(Context context) {
        this.context = context;
        this.focus = new FocusManager(context);
    }

    public boolean isPlaying() {
        for(Player p : players) {
            if(Utils.isPlaying(p.getState())) return true;
        }
        return false;
    }

    public boolean isPlayingLocal() {
        for(Player p : players) {
            if(p instanceof RemotePlayer) continue;
            if(Utils.isPlaying(p.getState())) return true;
        }
        return false;
    }

    public void onPlay(Player p) {
        if(!(p instanceof RemotePlayer)) {
            focus.enable();
        }
    }

    public void onStop() {
        if(!isPlayingLocal()) {
            focus.disable();
        }
    }

}
