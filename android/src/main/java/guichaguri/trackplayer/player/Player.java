package guichaguri.trackplayer.player;

import android.content.Context;
import android.os.SystemClock;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.track.Track;
import guichaguri.trackplayer.player.components.PlayerView;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @author Guilherme Chaguri
 */
public abstract class Player<T extends Track> {

    protected final Context context;
    protected final MediaManager manager;
    protected LinkedList<T> queue = new LinkedList<>();
    protected int currentTrack = 0;

    private int prevState = 0;

    protected Player(Context context, MediaManager manager) {
        this.context = context;
        this.manager = manager;
    }

    protected abstract T createTrack(ReadableMap data);

    public void add(int index, T track) throws Exception {
        if(index < 0) {
            queue.add(track);
        } else {
            queue.add(index, track);
        }
    }

    public  void add(int index, ReadableMap data) throws Exception {
        add(index, createTrack(data));
    }

    public void remove(String[] ids) throws Exception {
        ListIterator<T> i = queue.listIterator();
        boolean trackChanged = false;

        while(i.hasNext()) {
            int index = i.nextIndex();
            Track track = i.next();
            for(String id : ids) {
                if(track.id.equals(id)) {
                    i.remove();
                    if(currentTrack == index) {
                        currentTrack = i.nextIndex();
                        trackChanged = true;
                    }
                    break;
                }
            }
        }

        if(trackChanged) updateCurrentTrack(null);
    }

    public void skip(String id, Callback callback) throws Exception {
        for(int i = 0; i < queue.size(); i++) {
            Track track = queue.get(i);
            if(track.id.equals(id)) {
                currentTrack = i;
                updateCurrentTrack(callback);
                return;
            }
        }

        Utils.triggerCallback(callback);
    }

    public void skipToNext(Callback callback) throws Exception {
        if(currentTrack < queue.size() - 1) {
            currentTrack++;
            updateCurrentTrack(callback);
        } else {
            Utils.triggerCallback(callback);
        }
    }

    public void skipToPrevious(Callback callback) throws Exception {
        if(currentTrack > 0) {
            currentTrack--;
            updateCurrentTrack(callback);
        } else {
            Utils.triggerCallback(callback);
        }
    }

    /**
     * Custom data
     */
    public abstract void update(ReadableMap data, Callback updateCallback);

    public abstract void load(T track, Callback loadCallback) throws IOException;

    public void load(ReadableMap data, Callback loadCallback) throws IOException {
        load(createTrack(data), loadCallback);
    }

    public abstract void reset() throws Exception;

    public abstract void play() throws Exception;

    public abstract void pause() throws Exception;

    public abstract void stop() throws Exception;

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

    protected void updateCurrentTrack(Callback callback) throws Exception {
        int state = getState();
        T track = queue.get(currentTrack);

        load(track, callback);

        if(Utils.isPlaying(state)) {
            play();
        } else if(Utils.isPaused(state)) {
            pause();
        }
    }
}
