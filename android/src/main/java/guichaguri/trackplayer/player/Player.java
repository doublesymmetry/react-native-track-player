package guichaguri.trackplayer.player;

import android.content.Context;
import android.os.SystemClock;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.metadata.Metadata;
import java.io.IOException;

/**
 * @author Guilherme Chaguri
 */
public abstract class Player {

    protected final Context context;
    private Metadata metadata;

    protected Player(Context context) {
        this.context = context;
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

    public final void bindMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    protected final void updateMetadata() {
        if(metadata != null) metadata.updatePlayback();
    }

}
