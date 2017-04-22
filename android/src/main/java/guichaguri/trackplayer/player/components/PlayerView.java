package guichaguri.trackplayer.player.components;

import android.content.Context;
import android.view.SurfaceView;
import guichaguri.trackplayer.logic.components.VideoWrapper;

/**
 * A view using {@link SurfaceView} for videos
 *
 * @author Guilherme Chaguri
 */
public class PlayerView extends SurfaceView {

    private int boundPlayer = -1;

    public PlayerView(Context context) {
        super(context);
        setKeepScreenOn(true);
    }

    public void bindPlayer(VideoWrapper video, int player) {
        // Unbind the old player
        if(video != null && boundPlayer != -1) {
            video.setView(boundPlayer, null);
        }

        this.boundPlayer = player;

        // Bind the new player
        if(video != null && boundPlayer != -1) {
            video.setView(boundPlayer, this);
        }
    }

    public void updatePlayer(VideoWrapper video) {
        // Update the player
        if(video != null && boundPlayer != -1) {
            video.setView(boundPlayer, this);
        }
    }
}
