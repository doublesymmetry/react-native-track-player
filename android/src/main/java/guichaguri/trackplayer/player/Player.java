package guichaguri.trackplayer.player;

import android.content.Context;
import android.os.SystemClock;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
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

    public abstract void load(ReadableMap data, Callback loadCallback) throws IOException;

    public abstract void play();

    public abstract void pause();

    public abstract void stop();

    /**
     * State from {@link android.support.v4.media.session.PlaybackStateCompat}
     */
    public abstract int getState();

    public abstract long getPosition();

    public long getPositionUpdateTime() {
        return SystemClock.elapsedRealtime();
    }

    public abstract long getBufferedPosition();

    public abstract long getDuration();

    public abstract void seekTo(int ms);

    public abstract float getSpeed();

    public abstract void setVolume(float volume);

    public abstract void destroy();

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
