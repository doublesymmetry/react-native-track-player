package guichaguri.trackplayer.player;

import android.content.Context;
import android.os.SystemClock;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.player.components.PlayerView;
import java.io.IOException;

/**
 * @author Guilherme Chaguri
 */
public abstract class Player {

    protected final Context context;
    protected final MediaManager manager;
    private int prevState = 0;

    protected Player(Context context, MediaManager manager) {
        this.context = context;
        this.manager = manager;
    }

    /**
     * Custom data
     */
    public abstract void update(ReadableMap data, Callback updateCallback);

    public abstract void load(ReadableMap data, Callback loadCallback) throws IOException;

    public abstract void reset() throws Exception;

    public abstract void play() throws Exception;

    public abstract void pause() throws Exception;

    public abstract void stop() throws Exception;

    /**
     * State from {@link android.support.v4.media.session.PlaybackStateCompat}
     */
    public abstract int getState();

    public abstract long getPosition() throws Exception;

    public long getPositionUpdateTime() {
        return SystemClock.elapsedRealtime();
    }

    public abstract long getBufferedPosition();

    public abstract long getDuration() throws Exception;

    public abstract void seekTo(long ms) throws Exception;

    public abstract float getSpeed();

    public abstract void setVolume(float volume) throws Exception;

    public abstract void bindView(PlayerView view);

    public abstract void destroy() throws Exception;

    protected final void updateState(int state) {
        updateMetadata();

        if(Utils.isPlaying(state) && !Utils.isPlaying(prevState)) {
            manager.onPlay(this);
        } else if(Utils.isPaused(state) && !Utils.isPaused(prevState)) {
            manager.onPause(this);
        } else if(Utils.isStopped(state) && !Utils.isStopped(prevState)) {
            manager.onStop(this);
        }

        prevState = state;
    }

    protected final void updateMetadata() {
        manager.onUpdate(this);
    }

}
