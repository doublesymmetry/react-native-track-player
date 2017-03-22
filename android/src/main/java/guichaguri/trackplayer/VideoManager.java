package guichaguri.trackplayer;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import guichaguri.trackplayer.player.view.PlayerView;

/**
 * @author Guilherme Chaguri
 */
public class VideoManager extends SimpleViewManager<PlayerView> {

    private final TrackModule module;

    private int boundPlayer = -1;

    public VideoManager(TrackModule module) {
        this.module = module;
    }

    @Override
    public String getName() {
        return "PlayerView";
    }

    @Override
    protected PlayerView createViewInstance(ThemedReactContext context) {
        return new PlayerView(context);
    }

    @Override
    public void onDropViewInstance(PlayerView view) {
        // Unbind the player
        if(boundPlayer != -1) {
            module.setView(boundPlayer, null);
        }
    }

    @ReactProp(name = "player", defaultInt = -1)
    public void setPlayer(PlayerView view, int id) {
        // Unbind the old player
        if(boundPlayer != -1) {
            module.setView(boundPlayer, null);
        }

        // Bind the new player
        if(id != -1) {
            module.setView(id, view);
        }

        boundPlayer = id;
    }
}
